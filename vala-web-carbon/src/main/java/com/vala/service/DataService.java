package com.vala.service;


import cn.hutool.core.util.ArrayUtil;
import com.vala.framework.utils.ExcelUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class DataService {
    private String dataPath;
    private String maxPath;

    public DataService(String dataPath, String maxPath){
        this.dataPath = dataPath;
        this.maxPath = maxPath;
    }





    public double[][] t(double[][] data){
        int len = data[0].length;
        double[][] ret = new double[len][data.length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                ret[j][i] = data[i][j];
            }
        }
        return ret;
    }

    private double parseDouble(Object o ){
        double ret = 0.0;
        if(o==null) return ret;
        if(o instanceof Double) return (Double)o;
        try {
            return Double.parseDouble(String.valueOf(o).trim());
        }catch (Exception e){
            return ret;
        }
    }

    // excel主体部分的数据，hang1,lie2
    private double[][] getRawData() throws Exception {
        File dataFile = new File(dataPath);
        List<Object[]> xlsx = ExcelUtils.read(new FileInputStream(dataFile), "xlsx");
        int rowCount = xlsx.size();
        double[][] temp = new double[rowCount][];
        for (int i = 0; i < rowCount; i++) {
            Object[] row = xlsx.get(i);
            int len = row.length;
            double[] rowData = new double[len];
            for (int j = 0; j < len; j++) {
                Object s = row[j];
                double d = parseDouble(s);
                rowData[j] = d;
            }
            temp[i] = rowData;
        }
        return temp;
    }
    private double[] getMaxes()throws Exception{
        if(maxPath==null) return null;
        File maxFile = new File(maxPath);
        List<Object[]> xlsx = ExcelUtils.read(new FileInputStream(maxFile), "xlsx");
        Object[] row = xlsx.get(0);
        double[] maxes = new double[row.length];
        for (int i = 0; i < row.length; i++) {
            Object o = row[i];
            maxes[i] = parseDouble(o);
        }
        return maxes;
    }

    // 滚动终止年份 - 滚动数据
    private Map<String,double[][]> divide(int start, int window, double[][] data) throws Exception {
        int year = start + window - 1;
        Map<String,double[][]> map = new LinkedHashMap<>();
        for (int i = 0; i < data.length-window+1; i++) {
            double[][] sub = new double[window][];
            String Year = (year+i)+"";
            for (int j = 0; j < window; j++) {
                sub[j] = data[j+i];
            }
            map.put(Year,sub);
        }
        return map;
    }


    public Map<String,double[][]>  getData(int start, int window) throws Exception {
        double[][] data = getRawData();
        double[] maxes = getMaxes();
        // 对data做归一化
        for (int i = 0; i < data.length; i++) {
            double[] line = data[i];
            for (int j = 0; j < line.length; j++) {
                double max = maxes==null? 1: maxes[j];
                data[i][j] = line[j]/max;
            }
        }
        // ？？
        return this.divide(start,window,data);
    }




    private double[][] getRawData0() throws Exception {
        File dataFile = new File("/Users/liuyinpeng/Documents/1月/碳演示/tang/data.xlsx");
        List<Object[]> xlsx = ExcelUtils.read(new FileInputStream(dataFile), "xlsx");
        int rowCount = xlsx.size();
        double[][] temp = new double[rowCount][];
        int colCount = 0;
        for (int i = 0; i < rowCount; i++) {
            Object[] row = xlsx.get(i);
            int len = row.length;
            colCount = len > colCount ? len : colCount;
            double[] rowData = new double[len];
            for (int j = 0; j < len; j++) {
                Object s = row[j];
                double d = parseDouble(s);
                rowData[j] = d;
            }
            temp[i] = rowData;
        }
        double[][] data = new double[colCount][rowCount];
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount ; j++) {
                data[j][i] = temp[i][j];
            }
        }
        return data;
    }



}
