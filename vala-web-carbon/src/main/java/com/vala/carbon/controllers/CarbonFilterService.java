package com.vala.carbon.controllers;

import cn.hutool.core.util.ArrayUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vala.base.entity.BaseEntity;
import com.vala.base.service.BaseService;
import com.vala.base.service.FastDfsService;
import com.vala.base.utils.TreeUtils;
import com.vala.framework.data.bean.DataBean;
import com.vala.framework.data.bean.DataFrameTreeBean;
import com.vala.framework.data.bean.DataItemBean;
import com.vala.framework.utils.ExcelUtils;
import com.vala.service.RService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.util.*;

@Service
public class CarbonFilterService {

    @Autowired
    private SimpMessagingTemplate messageTemplate;

    @Resource
    private BaseService<CarbonFilterEntity> baseService;

    @Autowired
    public FastDfsService fastDfsService;

    private double [] collectTreeData(List<DataFrameTreeBean> tree, int length ){
        double [] sum = this.initArray(length,0.0);

        for (DataFrameTreeBean node : tree) {
            Integer nodeType = node.nodeType;
            double[] temp = null;
            if(nodeType==1){
                temp = this.collectTreeData(node.children, length);
                node.setTempData(temp);
            }else{
                temp = node.getTempData();
                if(temp==null) continue;
            }
            for (int i = 0; i < sum.length; i++) {
                sum[i] += temp[i];
            }
        }
        return sum;
    }

    private void removeCore(List<DataFrameTreeBean> list, Integer coreId){
        DataFrameTreeBean core = null;
        for (DataFrameTreeBean bean : list) {
            if(bean.nodeType==2 && bean.getData().id.equals(coreId)){
                core = bean;
                break;
            }
        }
        list.remove(core);
    }

    public List<Object[]> getTreeData(CarbonFilterEntity filter) throws Exception {
        // 获得所有树型（含分类）
        DataFrameTreeBean treeBeanExample = new DataFrameTreeBean();
        treeBeanExample.frameId = filter.frame.id;
        List<DataFrameTreeBean> list = this.baseService.find(treeBeanExample);
        // 读取数据
        byte[] read = this.fastDfsService.read(filter.url);
        List<Object[]> xlsx = ExcelUtils.read(new ByteArrayInputStream(read), "xlsx");
        // 索引
        Map<Integer, DataFrameTreeBean> mapping = this.mapping(list);
        // 踢出core
        Integer coreId = filter.getFrame().getCore().getId();
        this.removeCore(list,coreId);
        // 在list中载入数据, 并获得categories
        List<String> categories = this.assignTreeData(mapping,xlsx);
        // 结果转为树形，将数据递归到一级节点（相加）
        List<DataFrameTreeBean> tree = TreeUtils.treeBean(list);
        this.collectTreeData(tree,categories.size());
        // 组装表头
        Object[] titles = new Object[tree.size()+1];
        for (int i = 0; i < titles.length-1; i++) {
            DataFrameTreeBean dataFrameTreeBean =  tree.get(i);
            titles[i+1] = dataFrameTreeBean.nodeType == 1 ? dataFrameTreeBean.name : dataFrameTreeBean.getData().name;
        }
        // 组装数据（第一行为表头）
        List<Object[]> data = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            String category =  categories.get(i);
            Object[] row = new Object[tree.size()+1];
            row[0] = category;
            for (int j = 0; j < tree.size(); j++) {
                DataFrameTreeBean dataFrameTreeBean =  tree.get(j);
                double[] tempData = dataFrameTreeBean.getTempData();
                row[j+1] = tempData[i];
            }
            data.add(row);
        }

        data.add(0,titles);

        return data;
    }

    /**
     * 将年份合并
     * @param data
     * @return
     */
    private List<Object[]> compress(List<Object[]> data){


        return null;
    }


    private List<String> assignTreeData(Map<Integer, DataFrameTreeBean> mapping, List<Object[]> xlsx){
        List<String> categories = new ArrayList<>();
        Map<Integer, double[]> map = new HashMap<>();
        Object[] titles = xlsx.get(0);
        for (int i = 1; i < xlsx.size(); i++) {
            Object[] objects = xlsx.get(i);
            String category = objects[0].toString();
            categories.add(category);
            for (int j = 1; j < objects.length; j++) {
                Double d = (Double) objects[j];
                Double title = (Double) titles[j];
                Integer id =title.intValue();
                double[] ds = null;
                if(map.containsKey(id)){
                    ds = map.get(id);
                }else{
                    ds = new double[xlsx.size()-1];
                    map.put(id,ds );
                }
                ds[i-1] = d;
            }
        }
        for (Integer id : map.keySet()) {
            double[] doubles = map.get(id);
            DataFrameTreeBean dataFrameTreeBean = mapping.get(id);
            dataFrameTreeBean.setTempData(doubles);
        }
        return categories;
    }






    @Async
    public void doit(CarbonFilterEntity filter ,  Integer uid) throws Exception {
        try {

            // 获得所有树型（含分类）
            DataFrameTreeBean treeBeanExample = new DataFrameTreeBean();
            treeBeanExample.frameId = filter.frame.id;
            treeBeanExample.nodeType = 2;
            List<DataFrameTreeBean> dataFrameTreeBeans = this.baseService.find(treeBeanExample);
            // 数据排序
            dataFrameTreeBeans.sort(Comparator.comparing(DataFrameTreeBean::getTimestamp).reversed());
            // 筛选数据类型的树形（非分组），并加载数据，同时做归一化，
            List<DataFrameTreeBean> dataList = this.loadData(dataFrameTreeBeans);
            for (DataFrameTreeBean dataFrameTreeBean : dataList) {
                String name = dataFrameTreeBean.getData().name;
                List<DataItemBean> items = dataFrameTreeBean.getDataItems();
                // 归一化
                this.unify(dataFrameTreeBean);
            }
            // 组装成原始数据，用核心数据的category做索引，并将core移动到排头。数据在double[]中依次排列。
            Integer coreId = filter.frame.getCore().id;
            Map<String, double[]> rawData = this.convert2RawData(dataList,coreId);
            // 获取窗口数据
            Integer window = filter.win;
            Map<String, double[][]> map = this.convert2WindowData(rawData, window);
            // 在列表中删除core
            DataFrameTreeBean core = dataList.get(0);
            dataList.remove(core);
            dataFrameTreeBeans.remove(core);



            long startTime = System.currentTimeMillis();

            // 开始计算,并保存结果
            List<Object[]> list = new ArrayList<>();
            RService rService = new RService();
            for (String category : map.keySet()) {
                double[][] data = map.get(category);// m: 指标数， n: 日期数
                double[] forest = rService.forest(data);
                Object[] os = new Object[forest.length+1];
                os[0] = category;
                for (int i = 0; i < forest.length; i++) {
                    os[i+1] = forest[i];
                }
                list.add(os);
            }
            rService.close();

            // 获取表头
            List titles = new ArrayList<>();
            for (DataFrameTreeBean dataFrameTreeBean : dataList) {
                titles.add(dataFrameTreeBean.id);
            }

            // 上传数据文件
            byte[] bytes = ExcelUtils.write(filter.name, titles, list);
            String url = fastDfsService.upload(bytes, "xlsx");

            // 计算时间
            long endTime = System.currentTimeMillis();
            int time = (int) (endTime-startTime)/1000;

            // 更新状态
            CarbonFilterEntity temp = this.baseService.get(CarbonFilterEntity.class,filter.id);
            temp.setState("calculated");
            temp.setExpired(false);
            temp.description = "运算耗时"+time+"秒";
            // 删除旧文件，保存新文件地址
            if(temp.url!=null){
                this.fastDfsService.delete(temp.url);
            }
            temp.url = url;
            // 更新数据库
            this.baseService.getRepo().save(temp);

            // 推送消息
            BaseEntity baseEntity = new BaseEntity();
            baseEntity.id = filter.id;
            baseEntity.code="true";
            if(uid!=null){
                ObjectMapper mapper = new ObjectMapper();
                String s = mapper.writeValueAsString(baseEntity);
                messageTemplate.convertAndSendToUser(uid.toString(), "/queue/points", s);
            }
        } catch (Exception e) {
            e.printStackTrace();
            CarbonFilterEntity temp = this.baseService.get(CarbonFilterEntity.class,filter.id);
            temp.setState("error");
            temp.setExpired(true);
            temp.description = "错误："+e.getMessage();
            this.baseService.getRepo().save(temp);

            BaseEntity baseEntity = new BaseEntity();
            baseEntity.id = filter.id;
            baseEntity.code="false";
            if(uid!=null){
                ObjectMapper mapper = new ObjectMapper();
                String s = mapper.writeValueAsString(baseEntity);
                messageTemplate.convertAndSendToUser(uid.toString(), "/queue/points", s);
            }

        }
    }


    public List<Object[]> getFullData(CarbonFilterEntity filter) throws Exception {
        // 检索所有数据节点（不含分类）
        DataFrameTreeBean treeBeanExample = new DataFrameTreeBean();
        treeBeanExample.frameId = filter.frame.id;
        treeBeanExample.nodeType = 2;
        List<DataFrameTreeBean> dataFrameTreeBeans = this.baseService.find(treeBeanExample);
        // 索引
        Map<Integer, DataFrameTreeBean> mapping = this.mapping(dataFrameTreeBeans);
        // 读取
        byte[] read = this.fastDfsService.read(filter.url);
        List<Object[]> xlsx = ExcelUtils.read(new ByteArrayInputStream(read), "xlsx");
        // 组装数据
        Object[] titles = xlsx.get(0);
        for (int i = 1; i < titles.length; i++) {
            Double title = (Double) titles[i];
            Integer id =title.intValue();
            DataFrameTreeBean dataFrameTreeBean = mapping.get(id);
            String name = dataFrameTreeBean.getData().name;
            titles[i] = name;
        }
        return xlsx;
    }


    /**
     * 索引
      * @param dataFrameTreeBeans
     * @return
     */
    private Map<Integer, DataFrameTreeBean> mapping(List<DataFrameTreeBean> dataFrameTreeBeans){
        Map<Integer, DataFrameTreeBean> map = new HashMap<>();
        for (DataFrameTreeBean dataFrameTreeBean : dataFrameTreeBeans) {
            map.put(dataFrameTreeBean.id, dataFrameTreeBean);
        }
        return map;
    }

    /**
     * 空数组
     * @param len
     * @param val
     * @return
     */
    private double[] initArray(int len, double val){
        double[] ds = new double[len];
        for (int i = 0; i < ds.length; i++) {
            ds[i] = val;
        }
        return ds;
    }

    /**
     * 筛选数据类型的树形（非分组），并加载数据
     * @param dataFrameTreeBeans
     * @return
     */
    private List<DataFrameTreeBean> loadData(List<DataFrameTreeBean> dataFrameTreeBeans){
        List<DataFrameTreeBean> ids = new ArrayList<>();
        for (DataFrameTreeBean tree : dataFrameTreeBeans) {
            if(tree.nodeType.equals(2)){
                DataBean data = tree.getData();
                // 加载数据
                List<DataItemBean> dataItems = this.getDataItems(data);
                tree.setDataItems(dataItems);

                ids.add(tree);
            }
        }
        return ids;
    }

    /**
     * 归一化
     */
    private void unify(DataFrameTreeBean tree){
        Double max = Double.NEGATIVE_INFINITY;
        List<DataItemBean> dataItems = tree.getDataItems();
        // 如果设定了最大值，就用设定的。否则动态计算。
        if(tree.max!=null){
            max = tree.max;
        }else{
            for (DataItemBean dataItem : dataItems) {
                Double value = dataItem.getValue();
                if(value>max){
                    max = value;
                }
            }
        }
        for (DataItemBean dataItem : dataItems) {
            Double value = dataItem.getValue()/max;
            // 防止数据冒出
            if(value>1) value = 1.0;
            dataItem.setValue(value);
        }
    }


    /**
     * 转成窗口数据
     * @param map
     * @param window
     * @return
     */

    private  Map<String, double[][]>  convert2WindowData(Map<String, double[]> map, Integer window ){
        List<String> keys = new ArrayList<>(map.keySet());
        Map<String, double[][]> windowMap = new LinkedHashMap<>();

        for (int i = window-1; i < keys.size(); i++) {
            String key =  keys.get(i);
            int len = map.get(key).length;
            double[][] sub = new double[len][window];
            for (int j = 0; j < window; j++) {
                String subKey =  keys.get(i-window+j+1);
                double[] value = map.get(subKey);
                for (int k = 0; k < value.length; k++) {
                    double d = value[k];
                    sub[k][j] = d;
                }
            }
            windowMap.put(key,sub);
        }
        return windowMap;
    }


    /**
     * 组装成原始数据矩阵，用核心数据（core）的category相作为索引
     * @return
     */
    private Map<String, double[]> convert2RawData(List<DataFrameTreeBean> list, Integer coreId){
        DataFrameTreeBean core = null;
        Map<String, double[]> map = new LinkedHashMap<>();
        for (DataFrameTreeBean dataFrameTreeBean : list) {
            if(dataFrameTreeBean.getData().id.equals(coreId)){
                core = dataFrameTreeBean;
                break;
            }
        }
        // 初始化缓存map，以便后续向内添加数据。
        if(core!=null){
            // 将core移动到排头
            list.remove(core);
            list.add(0,core);

            // 初始化缓存
            int length = list.size();
            List<DataItemBean> coreItems = core.getDataItems();
            for (DataItemBean coreItem : coreItems) {
                String category = coreItem.getCategory();
                map.put(category,new double[length]);
            }
        }
        //添加数据
        for (int i = 0; i < list.size(); i++) {
            DataFrameTreeBean dataFrameTreeBean =  list.get(i);
            List<DataItemBean> dataItems = dataFrameTreeBean.getDataItems();
            for (DataItemBean dataItem : dataItems) {
                String category = dataItem.getCategory();
                Double value = dataItem.getValue();
                if(map.containsKey(category)){
                    double[] ds = map.get(category);
                    ds[i] = value;
                }
            }
        }
        return map;
    }

    /**
     * 查找核心数据
     * @return
     */

    private DataFrameTreeBean getCoreData(List<DataFrameTreeBean> dataList, Integer coreId){
        for (DataFrameTreeBean dataFrameTreeBean : dataList) {
            if(dataFrameTreeBean.getData().id==coreId){
                return dataFrameTreeBean;
            }
        }
        return null;
    }


    /**
     * 加载数据
     * @param data
     * @return
     */
    private List<DataItemBean> getDataItems(DataBean data){
        DataItemBean example = new DataItemBean();
        DataBean temp = new DataBean();
        temp.setId(data.id);
        example.setData(temp);
        List<DataItemBean> dataItemBeans = this.baseService.find(example);
        dataItemBeans.sort(Comparator.comparing(DataItemBean::getCategory));
        return dataItemBeans;
    }
}
