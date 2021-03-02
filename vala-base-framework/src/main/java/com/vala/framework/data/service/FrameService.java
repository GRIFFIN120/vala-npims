package com.vala.framework.data.service;

import com.vala.base.service.BaseService;
import com.vala.commons.bean.data.VData;
import com.vala.framework.data.bean.DataBean;
import com.vala.framework.data.bean.DataFrameBean;
import com.vala.framework.data.bean.DataFrameCategoryEntity;
import com.vala.framework.data.bean.DataItemBean;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;
@Service
public class FrameService {

    @Resource
    private BaseService<DataFrameBean> baseService;

    static int CORE_INDEX;

    public VData getData(DataFrameBean frame, String titleFieldName, boolean withCore) throws Exception {
        List<String> categories = this.categories(frame.getId());
        List<DataBean> dataList = frame.getData();
        if(withCore){
            DataBean core = frame.getCore();
            dataList.add(CORE_INDEX,core);
        }
        VData vData = this.frameData(dataList, categories,titleFieldName);
        vData.name = frame.name;
        return vData;
    }

    public Map<Integer, DataBean> dataMapping(DataFrameBean frame){
        Map<Integer, DataBean> mapping = new HashMap<>();
        List<DataBean> data = frame.getData();
        for (DataBean datum : data) {
            Integer id = datum.getId();
            mapping.put(id,datum);
        }
        return mapping;
    }



    private List<String> categories(Integer frameId){
        DataFrameCategoryEntity example = new DataFrameCategoryEntity();
        example.setFrameId(frameId);
        example.setChecked(true);
        List<DataFrameCategoryEntity> list = this.baseService.find(example);
        list.sort(Comparator.comparing(DataFrameCategoryEntity::getTimestamp));
        List<String> categories = new ArrayList<>();
        for (DataFrameCategoryEntity dataFrameCategoryEntity : list) {
            String category = dataFrameCategoryEntity.getCategory();
            categories.add(category);
        }
        return categories;
    }


    private VData frameData(List<DataBean> data, List<String> categories , String titleFieldName) throws Exception {
        List titles = new ArrayList<>();
        Map<Object, Map<String, Double>> temp = new LinkedHashMap<>();
        for (DataBean datum : data) {
            Field field = DataBean.class.getField(titleFieldName);
            field.setAccessible(true);
            Object title = field.get(datum);
            Map<String, Double> map = new HashMap<>();

            // 获得data下的items
            DataItemBean exp = new DataItemBean();
            DataBean dTemp = new DataBean();
            dTemp.setId(datum.getId());
            exp.setData(dTemp);
            List<DataItemBean> items = baseService.find(exp);

            for (DataItemBean item : items) {
                String category = item.getCategory();
                Double value = item.getValue();
                map.put(category,value);
            }
            temp.put(title,map);
            titles.add(title);
        }
        VData vData = new VData(titles, categories);
        for (String category : categories) {
            for (Object title : titles) {
                Map<String, Double> map = temp.get(title);
                Double value = map.get(category);
                vData.setDataBy(title,category,value);
            }
        }
        return vData;
    }
}
