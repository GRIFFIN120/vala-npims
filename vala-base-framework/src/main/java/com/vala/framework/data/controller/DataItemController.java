package com.vala.framework.data.controller;

import com.vala.base.bean.SearchBean;
import com.vala.base.bean.SearchResult;
import com.vala.base.controller.BaseController;
import com.vala.commons.bean.ChartBean;
import com.vala.commons.bean.ResponseResult;
import com.vala.framework.data.bean.DataBean;
import com.vala.framework.data.bean.DataFrameBean;
import com.vala.framework.data.bean.DataItemBean;
import com.vala.framework.menu.entity.MenuItem;
import org.apache.commons.collections.ArrayStack;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/data-item")
public class DataItemController extends BaseController<DataItemBean> {

    @RequestMapping("/combine/{id}/{ids}")
    public ResponseResult combine(@PathVariable Integer id, @PathVariable List<Integer> ids) throws Exception {
        int len = ids.size();
        DataFrameBean dataFrameBean = this.baseService.get(DataFrameBean.class, id);
        if(dataFrameBean==null) return new ResponseResult(400,"视图不存在");

        List<String> titles = new ArrayStack();
        List<String> cates = new ArrayList<>();
        Map<String,Double[]> map = new LinkedHashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            Integer dataId = ids.get(i);
            DataBean dataBean = this.baseService.get(DataBean.class, dataId);
            if(dataBean==null) return new ResponseResult(400,"数据不存在:"+dataId);

            if((!dataBean.getDomain().equals(dataFrameBean.getDomain())) || (dataBean.getScaleId()!= dataFrameBean.getScaleId())) return new ResponseResult(400,"数据校验不通过");
            titles.add(dataBean.getName());
            List<DataItemBean> data = this.getData(dataId);
            for (DataItemBean datum : data) {
                String category = datum.getCategory();
                Double value = datum.getValue();
                Double[] arr = map.get(category);
                if(arr==null){
                    arr = new Double[len];
                    arr[i] = value;
                    map.put(category,arr);
                    cates.add(category);
                }else{
                    arr[i] = value;
                }
            }
        }


        if(dataFrameBean.getDomain().equalsIgnoreCase("series")){
            Collections.sort(cates, (a, b) -> {
                if (a.equals(b)) return 0;
                if (a.length() > b.length()) {
                    return 1;
                } else if (a.length() < b.length()) {
                    return -1;
                } else {
                    return a.compareTo(b);
                }
            });
            Map<String,Double[]> reMap = new LinkedHashMap<>();
            for (String cate : cates) {
                reMap.put(cate,map.get(cate));
            }
            map = reMap;
        }

        ChartBean chart = new ChartBean();
        chart.setTitles(titles);
        chart.setData(map);


        return new ResponseResult(200,"数据加载成功",chart);
    }

    private List<DataItemBean> getData(Integer dataId) throws Exception {
        DataItemBean exact = new DataItemBean();
        DataBean db = new DataBean();
        db.setId(dataId);
        exact.setData(db);
        SearchBean<DataItemBean> search = new SearchBean<>();
        search.setExact(exact);
        SearchResult<DataItemBean> result = this.baseService.search(search);

        List<DataItemBean> list = result.getList();
        return list;
    }

}
