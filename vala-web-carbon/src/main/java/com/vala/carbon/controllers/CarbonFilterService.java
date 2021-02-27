package com.vala.carbon.controllers;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.poi.excel.ExcelUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vala.base.entity.BaseEntity;
import com.vala.base.entity.TreeEntity;
import com.vala.base.service.BaseRepo;
import com.vala.base.service.BaseService;
import com.vala.base.service.FastDfsService;
import com.vala.base.utils.TreeUtils;
import com.vala.framework.data.bean.DataBean;
import com.vala.framework.data.bean.DataFrameTreeBean;
import com.vala.framework.data.bean.DataItemBean;
import com.vala.framework.utils.ExcelUtils;
import com.vala.service.RService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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

    public double[] initArray(int len, double val){
        double[] ds = new double[len];
        for (int i = 0; i < ds.length; i++) {
            ds[i] = val;
        }
        return ds;
    }

    @Async
    void doit(Map<String, double[][]> map, CarbonFilterEntity filter , List<DataFrameTreeBean> dataList, List<DataFrameTreeBean> dataFrameTreeBeans, Integer uid) {
        try {
            long startTime = System.currentTimeMillis();

            List<double[]> results = new ArrayList<>();
            List<String> categories = new ArrayList<>();
            RService rService = new RService();
            for (String category : map.keySet()) {
                double[][] data = map.get(category);// m: 指标数， n: 日期数
                double[] forest = rService.forest(data);
                results.add(forest);
                categories.add(category);
            }
            rService.close();

            // 将结果保存在节点中（节点已去除core，所以是按数据的顺序排列的）
            for (int i = 0; i < dataList.size(); i++) {
                DataFrameTreeBean dataFrameTreeBean =  dataList.get(i);
                double[] result = new double[results.size()];
                for (int j = 0; j < results.size(); j++) {
                    double[] doubles =  results.get(j);
                    result[j] = doubles[i];
                }
                dataFrameTreeBean.setTempData(result);
            }
            // 结果转为树形，将数据递归到一级节点（相加）
            List<DataFrameTreeBean> tree = TreeUtils.treeBean(dataFrameTreeBeans);
            this.collectTreeData(tree,categories.size());

            // 将数据从树形中去除，准备保存到excel中
            List titles = new ArrayList<>(); // 用tree的主键当做title，避免后面去数据时顺序错乱
            for (DataFrameTreeBean dataFrameTreeBean : tree) {
                titles.add(dataFrameTreeBean.id);
            }
            List<Object[]> list = new ArrayList<>();
            for (int i = 0; i < categories.size(); i++) {
                String category =  categories.get(i);
                Object[] row = new Object[tree.size()+1];
                row[0] = category;
                for (int j = 0; j < tree.size(); j++) {
                    DataFrameTreeBean dataFrameTreeBean =  tree.get(j);
                    double[] tempData = dataFrameTreeBean.getTempData();
                    row[j+1] = tempData[i];
                }
                list.add(row);
            }


//            // 上传数据文件
            byte[] bytes = ExcelUtils.write(filter.name, titles, list);
            String url = fastDfsService.upload(bytes, "xlsx");

            // 计算文件时间
            long endTime = System.currentTimeMillis();
            int time = (int) (endTime-startTime)/1000;

            // 更新状态
            CarbonFilterEntity temp = this.baseService.get(CarbonFilterEntity.class,filter.id);
            temp.setState("calculated");
            temp.setExpired(false);
            temp.description = "运算耗时"+time+"秒";
            // 删除旧文件
            if(temp.url!=null){
                this.fastDfsService.delete(temp.url);
            }
            temp.url = url;

            this.baseService.getRepo().save(temp);

            BaseEntity baseEntity = new BaseEntity();
            baseEntity.id = filter.id;

            // 推送消息
            if(uid!=null){
                messageTemplate.convertAndSendToUser(uid.toString(), "/queue/points", baseEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            CarbonFilterEntity temp = this.baseService.get(CarbonFilterEntity.class,filter.id);
            temp.setState("error");
            temp.setExpired(true);
            temp.description = "错误："+e.getMessage();
            this.baseService.getRepo().save(temp);
        }
    }


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


}
