package com.vala.carbon.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vala.base.entity.BaseEntity;
import com.vala.base.service.BaseService;
import com.vala.base.service.FastDfsService;
import com.vala.base.utils.TreeUtils;
import com.vala.carbon.controllers.entity.CarbonFilterEntity;
import com.vala.commons.bean.data.VData;
import com.vala.framework.data.bean.DataFrameBean;
import com.vala.framework.data.bean.DataFrameTreeBean;
import com.vala.framework.data.service.FrameService;
import com.vala.framework.utils.ExcelUtils;
import com.vala.service.RService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.*;

@Service
public class ForecastService {
    @Autowired
    FastDfsService fastDfsService;

    @Autowired
    BaseService<CarbonFilterEntity> baseService;

    @Autowired
    FilterService filterService;

    @Autowired
    FrameService frameService;

    @Autowired
    private SimpMessagingTemplate messageTemplate;

    @Async
    public void handler(Integer filterId,Integer uid) throws Exception {

        CarbonFilterEntity filterEntity = this.baseService.get(CarbonFilterEntity.class,filterId);

        // 预测长度
        int size = filterEntity.h;
        //arima 参数
        int[] c = new int[]{filterEntity.ar,filterEntity.i,filterEntity.ma};
        // 获取树形,索引,建立结构
        List<DataFrameTreeBean> treeList = this.filterService.getTreeList(filterEntity);
        Map<Integer, DataFrameTreeBean> mapping = this.filterService.treeDataMapping(treeList);
        List<DataFrameTreeBean> tree = TreeUtils.treeBean(treeList);
        // -------------------加载A
        VData A = this.A_loading(filterEntity);
        // 预测A, 并把预测值放在首位
        this.A_forcast(A,mapping,size);

        // -------------------加载P
        VData P = filterService.getDataFromFileField(filterEntity, "url");
        // 预测P, 并把预测值放在首位
        this.P_forecast(P,c,size);
        // 归一化P
        this.P_unify(P,tree);// ?check 横向归一化
//        P.unify(null); // ?check  纵向归一化


        // -------------------加载V
        DataFrameBean frame = filterEntity.getFrame();
        VData V = frameService.getData(frame,"id",false);
        // 预测V，把预测值放在首位
        this.V_forecast(V,mapping,size);


        // 计算X (第一行为2030数据)
        List titles = P.titles;
        List<String> categories = P.categories;
        VData X = new VData(titles,categories);
        for (String category : categories) {
            for (Object title : titles) {
                Double p = P.getDataBy(title,category);
                Double v = V.getDataBy(title,category);
                Double a = A.getDataBy(title,category);
                Double x = p*v*a;
                X.setDataBy(title,category,x);
            }
        }
        filterService.assembleTreeList(treeList,X);
        tree.sort(Comparator.comparing(DataFrameTreeBean::getTimestamp).reversed());
        this.filterService.sumTreeData(tree, X.ROW_COUNT, false);




        VData xi = filterService.tree2data(tree,X.getCategories());
        VData xp = xi.subRowData(0);
        xi.removeRowAt(0);

        List<Object[]> xiPlain = xi.toPlainData();
        List<Object[]> xpPlain = xp.toPlainData();

        byte[] xiBytes = ExcelUtils.write("因子耦合结果(历史)", xiPlain);
        byte[] xpBytes = ExcelUtils.write("因子耦合结果（预测）", xpPlain);

        String xiPath = this.fastDfsService.upload(xiBytes, "xlsx");
        String xpPath = this.fastDfsService.upload(xpBytes, "xlsx");

        if(filterEntity.xiPath!=null){
            this.fastDfsService.delete(filterEntity.xiPath);
        }
        if(filterEntity.xpPath!=null){
            this.fastDfsService.delete(filterEntity.xpPath);
        }

        filterEntity.xiPath = xiPath;

        filterEntity.xpPath = xpPath;


        this.baseService.getRepo().save(filterEntity);
        this.sendMsg(filterId,uid);
    }


    public void sendMsg(Integer filterId, Integer uid) throws JsonProcessingException {
        BaseEntity baseEntity = new BaseEntity();
        baseEntity.id = filterId;
        baseEntity.code="true";
        if(uid!=null){
            ObjectMapper mapper = new ObjectMapper();
            String s = mapper.writeValueAsString(baseEntity);
            messageTemplate.convertAndSendToUser(uid.toString(), "/queue/points", s);
        }
    }

    private String getForeYear(VData data, int size){
        List<String> categories = data.categories;
        String last = categories.get(data.ROW_COUNT-1);
        Integer year = Integer.valueOf(last);
        String foreYear = String.valueOf(year + size);
        return foreYear;
    }

    public void P_unify(VData P, List<DataFrameTreeBean> tree ){
        Map<Integer, List> groups = new HashMap<>();
        this.traceTreeGroup(tree,groups);
        for (Integer pid : groups.keySet()) {
            List group = groups.get(pid);
            Double[] sum = P.sum_columns(group);//
            for (Object title : group) {
                Double[] column = P.getColumnBy(title);
                for (int i = 0; i < P.ROW_COUNT; i++) {
                    String category = P.categories.get(i);
                    Double val = column[i];
                    Double div = sum[i];
                    Double value = div==0.0? 0.0: val/div;
                    P.setDataBy(title,category,value);
                }
            }
        }
    }

    private void traceTreeGroup(List<DataFrameTreeBean> tree, Map<Integer, List> groups){
        for (DataFrameTreeBean node : tree) {
            if(node.nodeType==1){
                Integer nodeId = node.id;
                groups.put(nodeId,new ArrayList());
                traceTreeGroup(node.getChildren(),groups);
            }else{
                Integer pid = node.pid;
                Integer dataId = node.getData().getId();
                if(pid!=0){
                    groups.get(pid).add(dataId);
                }
            }
        }
    }


    public void P_forecast(VData P, int[] c, int size) throws Exception {
        String foreYear = this.getForeYear(P,size);
        RService rService = new RService();
        Double[] forecast = new Double[P.COL_COUNT];
        for (int i = 0; i < P.COL_COUNT; i++) {
            Double[] column = P.getColumnAt(i);
            Double d = rService.forecast(column, c, size);
            forecast[i] = d;
        }
        rService.close();
        P.addRowAt(0,foreYear,forecast);
    }

    public void V_forecast(VData V, Map<Integer, DataFrameTreeBean> mapping, int size){
        String foreYear = this.getForeYear(V,size);
        Double[] forecast = new Double[V.COL_COUNT];
        for (int i = 0; i < V.COL_COUNT; i++) {
            Object title = V.titles.get(i);
            Integer dataId = (Integer) title;
            DataFrameTreeBean treeBean = mapping.get(dataId);
            Double predict = treeBean.getPredict();
            forecast[i] = predict;
        }
        V.addRowAt(0,foreYear,forecast);
    }

    public void A_forcast(VData A, Map<Integer, DataFrameTreeBean> mapping, int size){
        String foreYear = this.getForeYear(A,size);
        Double[] forecast = new Double[A.COL_COUNT];
        List<Integer> titles = A.titles;
        for (int i = 0; i < titles.size(); i++) {
            Integer dataId =  titles.get(i);
            DataFrameTreeBean treeBean = mapping.get(dataId);
            Double parameter = treeBean.getParameter();
            forecast[i] = parameter;
        }
        A.addRowAt(0,foreYear,forecast);
    }

    public VData A_loading(CarbonFilterEntity filter) throws Exception {
        String paramPath = filter.getParamPath();
        byte[] read = this.fastDfsService.read(paramPath);
        List<Object[]> xlsx = ExcelUtils.read(new ByteArrayInputStream(read), "xlsx");
        List titles = new ArrayList();
        Object[] ts = xlsx.get(0);
        for (int i = 1; i < ts.length; i++) {
            String str = (String)ts[i];
            String[] split = str.split(":");
            Integer dataId = Integer.valueOf(split[0]);
            titles.add(dataId);
        }

        VData data = new VData();
        data.titles = titles;
        data.COL_COUNT = titles.size();

        List<String> categories = new ArrayList<>();
        Map<String,Double[]> body = new HashMap<>();

        for (int i = 1; i < xlsx.size() ; i++) {
            Object[] rs = xlsx.get(i);
            String category = (String) rs[0];
            if(StringUtils.isEmpty(category)) continue;
            categories.add(category);
            Double[] values = new Double[data.COL_COUNT];
            for (int j = 1; j < rs.length; j++) {
                Object v = rs[j];
                Double value = v==null? 0.0 : Double.parseDouble(v.toString());
                values[j-1] = value;
            }
            body.put(category,values);
        }

        data.categories = categories;
        data.ROW_COUNT = categories.size();
        data.body = body;

        return data;
    }

}
