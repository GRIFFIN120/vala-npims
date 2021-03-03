package com.vala.carbon.service;

import com.vala.base.service.BaseService;
import com.vala.base.service.FastDfsService;
import com.vala.carbon.controllers.entity.CarbonFilterEntity;
import com.vala.commons.bean.data.VData;
import com.vala.framework.data.bean.DataFrameBean;
import com.vala.framework.data.bean.DataFrameTreeBean;
import com.vala.framework.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.*;

@Service
public class FilterService {
    @Autowired
    FastDfsService fastDfsService;

    @Resource
    private BaseService<DataFrameBean> baseService;


    public VData getDataFromFileField(CarbonFilterEntity filter, String fieldName) throws Exception {
        Field field = CarbonFilterEntity.class.getField(fieldName);
        field.setAccessible(true);
        String url = (String) field.get(filter);
        byte[] read = this.fastDfsService.read(filter.url);
        List<Object[]> xlsx = ExcelUtils.read(new ByteArrayInputStream(read), "xlsx");
        VData data = new VData(xlsx);
        List titles = data.getTitles();
        List<Integer> nTitles = new ArrayList<>();
        for (Object title : titles) {
            Double t = new Double(title.toString());
            nTitles.add(t.intValue());
        }
        data.setTitles(nTitles);
        return data;
    }

    public Map<Integer, DataFrameTreeBean> treeDataMapping(List<DataFrameTreeBean> treeList){
        Map<Integer, DataFrameTreeBean> mapping = new HashMap<>();
        for (DataFrameTreeBean treeBean : treeList) {
            if(treeBean.nodeType==2){
                Integer dataId = treeBean.getData().getId();
                mapping.put(dataId, treeBean);
            }
        }
        return mapping;
    }


    public List<DataFrameTreeBean> getTreeList(CarbonFilterEntity filterEntity){
        DataFrameTreeBean treeBeanExample = new DataFrameTreeBean();
        treeBeanExample.frameId = filterEntity.frame.id;
        List<DataFrameTreeBean> list = this.baseService.find(treeBeanExample);
        return list;
    }

    /**
     *  组装树形
     * @param treeList
     * @param data
     */
    public void assembleTreeList(List<DataFrameTreeBean> treeList, VData data){
        Map<Integer, DataFrameTreeBean> mapping = new HashMap<>();
        for (DataFrameTreeBean treeBean : treeList) {
            // 分类节点跳过
            if(treeBean.nodeType==1) continue;;
            Integer dataId = treeBean.getData().getId();
            // 非核心数据节点，将数据存放到tempData中。
            Integer title = dataId.intValue();
            Double[] columnBy = data.getColumnBy(title);
            treeBean.setTempData(columnBy);
            mapping.put(treeBean.id, treeBean);
        }
    }


    public VData tree2data(List<DataFrameTreeBean> tree, List<String> categories){
        VData tData = new VData();
        tData.categories.addAll(categories);
        for (DataFrameTreeBean treeBean : tree) {
            tData.titles.add(treeBean.getName());
        }
        tData.init();
        for (DataFrameTreeBean treeBean : tree) {
            String title = treeBean.getName();
            Double[] tempData = treeBean.getTempData();
            for (int i = 0; i < tempData.length; i++) {
                tData.setColumnBy(title,tempData);
            }
        }
        return tData;
    }


    public Double [] sumTreeData(List<DataFrameTreeBean> tree, int catLength , boolean isAve){
        Double [] sum = new Double[catLength];
        for (DataFrameTreeBean node : tree) {
            Integer nodeType = node.nodeType;
            Double[] tempData = null;
            if(nodeType==1){
                tempData = this.sumTreeData(node.children, catLength, isAve);
                node.setTempData(tempData);
            }else{
                tempData = node.getTempData();
                if(tempData==null) continue;
            }
            for (int i = 0; i < sum.length; i++) {
                Double temp = sum[i] == null? 0.0 : sum[i];
                sum[i] = temp + tempData[i];
            }
        }
        if(isAve){
            for (int i = 0; i < sum.length; i++) {
                Double temp = sum[i] == null? 0.0 : sum[i];
                sum[i] = sum[i]/tree.size();
            }
        }
        return sum;
    }
}
