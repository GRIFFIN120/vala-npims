package com.vala.demo.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.vala.commons.bean.ChartBean;
import com.vala.commons.bean.ResponseResult;
import com.vala.framework.data.bean.DataItemBean;
import com.vala.framework.utils.ExcelUtils;
import com.vala.service.DataService;
import com.vala.service.RService;
import net.sf.json.JSONObject;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {





    public String t1(){
        return "刘寅鹏";
    }

    @RequestMapping("/t2/{id}")
    public ResponseResult test2(@PathVariable Integer id) throws Exception {
        Map<String,List<DataItemBean>> map = this.getMap(id);


        for (String s : map.keySet()) {
            String str = CollectionUtil.join(map.get(s),",");
            System.out.println(s+","+str);
        }


        String[][] matrix = new String[][]{
            new String[]{"X1", "X2", "X3"},
            new String[]{"X4", "X5", "X6","X7", "X8", "X9","X10", "X11"},
            new String[]{"X12", "X13", "X14","X15", "X16", "X17","X18", "X19", "X20", "X21"},
            new String[]{"X22", "X23", "X24","X25", "X26", "X27"},
            new String[]{"X28","X29","X30","X31","X32","X33","X34","X35","X36","X37","X38","X39","X40","X41","X42"},
            new String[]{"X43","X44","X45","X46","X47","X48","X49","X50","X51"},
            new String[]{"X52","X53","X54"}
        };
        String[] titles = new String[]{
          "化石能源","非化石能源","高能耗产业","服务业","技术进步","居民传统消费","居民新兴消费"
        };

        Map<String,Double[]> result = new LinkedHashMap<>();

        int len = titles.length;
        for (int i = 0; i < titles.length; i++) {
            String title = titles[i];
            String[] keys = matrix[i];
            List<DataItemBean> dataItemBeans = this.meanY(map, keys);
            for (DataItemBean dataItemBean : dataItemBeans) {
                String cat = dataItemBean.getCategory();
                Double val = dataItemBean.getValue();
                Double[] vs = result.get(cat);
                if(vs==null){
                    vs = new Double[len];
                    result.put(cat,vs);
                }
                vs[i] = val;
            }
        }
        ChartBean chart = new ChartBean();
        chart.setData(result);
        chart.setTitles(CollectionUtil.toList(titles));

        return new ResponseResult(chart);
    }




    public List<DataItemBean> meanY(Map<String,List<DataItemBean>> MAP, String... keys){
        int len = keys.length;
        Map<String, Double> map = new LinkedHashMap<>();
        for (String key : keys) {
            List<DataItemBean> list = MAP.get(key);
            for (DataItemBean dataItemBean : list) {
                String cate = dataItemBean.getCategory();
                Double value = dataItemBean.getValue();
                if(map.get(cate)==null){
                    map.put(cate,value);
                }else{
                    Double sum = map.get(cate)+value;
                    map.put(cate, sum);
                }
            }
        }
        List<DataItemBean> list = new ArrayList<>();
        for (String s : map.keySet()) {
            Double d = map.get(s);
            DataItemBean data = new DataItemBean();
            data.setCategory(s);
            data.setValue(d/len);
            list.add(data);
        }
        return list;
    }




    public Map<String,List<DataItemBean>> getMap(Integer id) throws Exception{
        Map<String,List<DataItemBean>> map = new LinkedHashMap<>();

        File file = new File("/Users/liuyinpeng/Documents/1月/碳演示/tang/result"+id+".xlsx");
        List<Object[]> xlsx = ExcelUtils.read(new FileInputStream(file), "xlsx");

        Object[] titles = xlsx.get(0);
        for (int i = 0; i < titles.length; i++) {
            Object temp = titles[i];
            if(temp!=null){
                String title = (String) temp;
                List<DataItemBean> list = new ArrayList<>();
                for (int j = 1; j < xlsx.size(); j++) {
                    Object[] objects = xlsx.get(j);
                    double tt = (double) objects[0];
                    int year = (int) tt;
                    String category = String.valueOf(year);
                    Double vaule = (Double) objects[i];
                    DataItemBean data = new DataItemBean();
                    data.setCategory(category);
                    data.setValue(vaule);
                    list.add(data);
                }
                map.put(title,list);
            }
        }

        return map;
    }


    @RequestMapping("/t1")
    public void test() throws Exception {
        System.out.println(123);
    }

}
