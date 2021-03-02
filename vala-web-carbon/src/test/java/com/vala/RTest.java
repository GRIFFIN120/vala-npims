package com.vala;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.vala.service.DataService;
import com.vala.service.RService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RTest {
    @Test
    public void doit() throws Exception{
        String[] ns = new String[]{"AU1","FR1","IT1","JP1","UK1","US1"};
        int[] ss = new int[]{1978,1948,1973,1973,1948,1977};

//        String[] ns = new String[]{"CN5"};
//        int[] ss = new int[]{1960};
        int[] windows = new int[]{22};
        for (int i = 0; i < ss.length; i++) {
            String nation = ns[i];
            int start = ss[i];
//            int window = windows[i];
            int window = 22;
            test(nation,start,window);
        }

    }


    public void test(String nation, int start, int window) throws Exception {

//        String dataPath = "/Users/liuyinpeng/Documents/1月/碳演示/tang/data.xlsx";
//        String maxPath = "/Users/liuyinpeng/Documents/1月/碳演示/tang/max.xlsx";
//        String outPath = "/Users/liuyinpeng/Documents/1月/碳演示/tang/result7.csv";
//
//        String nation = "CN";
//        int start = 1960;

        String dataPath = "/Users/liuyinpeng/Documents/1月/碳演示/tang/国际/"+nation+".xlsx";
        String maxPath = "/Users/liuyinpeng/Documents/1月/碳演示/tang/max.xlsx";
        String outPath = "/Users/liuyinpeng/Documents/1月/碳演示/tang/国际/"+nation+"_"+window+"_result.csv";

        DataService dataService = new DataService(dataPath,maxPath);
        RService rService = new RService();

        Map<String, double[][]> map = dataService.getData(start, window);
        List<String> list = new ArrayList<>();
        for (String year : map.keySet()) {
            double[][] data = map.get(year);
            double[][] tdata = dataService.t(data);
            Double[] forest = rService.forest(tdata); // 调用R　 一个滚动窗做一次。
            String line = year+","+ArrayUtil.join(forest,",");
            System.out.println(line);
            list.add(line);
        }
        System.out.println("======");
        String str = CollectionUtil.join(list,"\r\n");
        System.out.println(str);
        FileUtils.write(new File(outPath),str,"utf-8");
        rService.close();
    }


}
