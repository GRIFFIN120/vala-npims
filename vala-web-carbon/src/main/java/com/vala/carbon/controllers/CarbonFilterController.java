package com.vala.carbon.controllers;

import cn.hutool.core.util.ArrayUtil;
import com.vala.base.service.FastDfsService;
import com.vala.commons.bean.ResponseResult;
import com.vala.framework.data.bean.DataBean;
import com.vala.framework.data.bean.DataFrameTreeBean;
import com.vala.framework.data.bean.DataItemBean;
import com.vala.framework.file.controller.FileBaseController;
import com.vala.framework.menu.entity.MenuItem;
import com.vala.framework.user.entity.UserBasic;
import com.vala.framework.utils.ExcelUtils;
import com.vala.service.RService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/carbon-filter")
public class CarbonFilterController extends FileBaseController<CarbonFilterEntity> {

    @Autowired
    CarbonFilterService service;

    @RequestMapping("/getResults")
    public ResponseResult exportResults(@RequestBody  CarbonFilterEntity filter) throws Exception {
        // 读取文件
        DataFrameTreeBean treeBeanExample = new DataFrameTreeBean();
        treeBeanExample.frameId = filter.frame.id;
        treeBeanExample.pid = 0;
        List<DataFrameTreeBean> dataFrameTreeBeans = this.baseService.find(treeBeanExample);

        // 索引
        Map<Integer, String> map = new HashMap<>();
        for (DataFrameTreeBean dataFrameTreeBean : dataFrameTreeBeans) {
            String name = dataFrameTreeBean.nodeType==1? dataFrameTreeBean.getName() :dataFrameTreeBean.getData().name;
            map.put(dataFrameTreeBean.id, name);
        }

        // 读取数据
        byte[] read = this.fastDfsService.read(filter.url);
        List<Object[]> xlsx = ExcelUtils.read(new ByteArrayInputStream(read), "xlsx");

        // 组装数据
        Object[] titles = xlsx.get(0);
        for (int i = 1; i < titles.length; i++) {
            Double title = (Double) titles[i];
            Integer id =title.intValue();
            titles[i] = map.get(id);
        }

        return new ResponseResult(xlsx);
    }


        @RequestMapping("/calculate")
    public ResponseResult calculate(@RequestBody  CarbonFilterEntity filter) throws Exception {


        // 获得所有树型（含分类）
        DataFrameTreeBean treeBeanExample = new DataFrameTreeBean();
        treeBeanExample.frameId = filter.frame.id;
        List<DataFrameTreeBean> dataFrameTreeBeans = this.baseService.find(treeBeanExample);
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

        // 开始计算 （异步操作）
        Integer uid = this.getSession("UID",Integer.class);
        service.doit(map,filter,dataList,dataFrameTreeBeans, uid);

        // 将数据状态更改为"运算中"
        CarbonFilterEntity temp = new CarbonFilterEntity();
        temp.id = filter.id;
        temp.setState("calculating");
        temp.setExpired(null);
        temp.setDescription("请等待...");
        this.baseService.saveOrUpdate(temp);

        return new ResponseResult(200,"后台运算中，完成运算后可查看结果。",temp);
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
        if(tree.parameter!=null){
            max = tree.parameter;
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

    @Override
    public void beforeInsert(CarbonFilterEntity ext) {
        ext.setState("prepare");
        Integer uid = this.getSession("UID", Integer.class);
        UserBasic userBasic = this.baseService.get(UserBasic.class, uid);
        ext.setUser(userBasic);
    }


}
