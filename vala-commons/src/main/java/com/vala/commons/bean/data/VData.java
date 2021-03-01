package com.vala.commons.bean.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@Data
public class VData {
    // 表头
    public List titles;

    // 类别项
    @JsonIgnore
    public List<String> categories;

    // 列数
    public int COL_COUNT;
    // 行数
    public int ROW_COUNT;

    // 中间存储时使用
    public Map<String, Double[]> body = new LinkedHashMap<>();

    // 数据名称
    public String name;

    /**
     * 构造函数
     * @param titles
     * @param categories
     */
    public VData(List<String> titles, List<String> categories){
        this.titles = titles;
        this.COL_COUNT = titles.size();
        this.categories = categories;
        this.ROW_COUNT = categories.size();
        for (String category : categories) {
            Double[] ds = new Double[titles.size()];
            body.put(category,ds);
        }
    }

    /**
     * 根据列号和行号录入数据
     * @param c
     * @param r
     */
    public Double getDataAt(int c, int r){
        Double[] row = this.getRowAt(r);
        return row[c];
    }

    /**
     * 根据列名和类别录入数据
     * @param title
     * @param category
     */
    public Double getDataBy(String title, String category){
        int c = titles.indexOf(title);
        int r = categories.indexOf(category);
        return this.getDataAt(c,r);
    }

    /**
     * 根据列号和行号录入数据
     * @param c
     * @param r
     * @param value
     */
    public void setDataAt(int c, int r, Double value){
        Double[] row = this.getRowAt(r);
        row[c] = value;
    }

    /**
     * 根据列名和类别录入数据
     * @param title
     * @param category
     * @param value
     */
    public void setDataBy(String title, String category, Double value){
        int c = titles.indexOf(title);
        int r = categories.indexOf(category);
        this.setDataAt(c,r,value);
    }

    /**
     * 根据行号获取一行数据
     * @param r
     * @return
     */
    public Double[] getRowAt(int r){
        String category = categories.get(r);
        return this.getRowBy(category);
    }


    /**
     * 根据类别项获取一行数据
     * @param category
     * @return
     */
    public Double[] getRowBy(String category){
        return body.get(category);
    }

    /**
     * 根据列号获取一列数据
     * @param c
     * @return
     */
    public Double[] getColumnAt(int c){
        Double[] column = new Double[ROW_COUNT];
        for (int i = 0; i < ROW_COUNT; i++) {
            String category = categories.get(i);
            Double[] row = body.get(category);
            Double value = row[c];
            column[i] = value;
        }
        return column;
    }

    /**
     * 根据列名获取一列数据
     * @param title
     * @return
     */
    public Double[] getColumnBy(String title){
        int c = titles.indexOf(title);
        return this.getColumnAt(c);
    }



    private double[] convert(Double[] values){
        double[] ds = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            ds[i] = values[i];
        }
        return ds;
    }


}
