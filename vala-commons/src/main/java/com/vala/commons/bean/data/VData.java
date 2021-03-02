package com.vala.commons.bean.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.*;

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


    public void unify(Double[] maxes){
        for (int c = 0; c <COL_COUNT; c++) {
            Double[] column = this.getColumnAt(c);
            Double max = maxes!=null&&maxes.length==COL_COUNT ? maxes[c] :null;
            if(max == null){
                max = Double.NEGATIVE_INFINITY;
                for (Double aDouble : column) {
                    max = aDouble!=null && aDouble>max ?  aDouble : max;
                }
            }
            for (int r = 0; r < ROW_COUNT; r++) {
                Double value = column[r]/max;
                if(value!=null) this.setDataAt(c,r,value);
            }
        }
    }

    public void show(){
        System.out.println(COL_COUNT+"*"+ROW_COUNT);
        for (String category : categories) {
            System.out.print(category+" : ");
            Double[] doubles = body.get(category);
            for (int i = 0; i < titles.size(); i++) {
                Object v = titles.get(i);
                Object t = doubles[i];
                System.out.print(v+"="+t+", ");
            }
            System.out.println();
        }
    }

    /**
     * 构造函数
     * @param titles
     * @param categories
     */
    public VData(List titles, List<String> categories){
        this.titles = titles;
        this.categories = categories;
        init();
    }

    public VData(){
        this.titles = new ArrayList();
        this.categories = new ArrayList<>();
    }

    public void init(){
        this.COL_COUNT = titles.size();
        this.ROW_COUNT = categories.size();
        for (String category : categories) {
            Double[] ds = new Double[titles.size()];
            body.put(category,ds);
        }
    }


    public VData(List<Object[]> xlsx){
        List titles = new ArrayList();
        Object[] x0 = xlsx.get(0);
        for (int i = 1; i < x0.length; i++) {
            Object title =  x0[i];
            titles.add(title);
        }
        this.titles = titles;
        this.COL_COUNT = titles.size();
        List<String> categories = new ArrayList<>();
        Map<String, Double[]> body = new LinkedHashMap<>();
        for (int i = 1; i < xlsx.size(); i++) {
            Object[] objects = xlsx.get(i);
            String category = objects[0].toString();
            Double[] values = new Double[objects.length-1];
            for (int j = 1; j < objects.length; j++) {
                Object o = objects[j];
                Double value = o == null ? null : Double.valueOf(o.toString());
                values[j-1] = value;
            }
            body.put(category,values);
            categories.add(category);
        }
        this.body = body;
        this.categories = categories;
        this.ROW_COUNT = categories.size();
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
    public Double getDataBy(Object title, String category){
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
    public void setDataBy(Object title, String category, Double value){
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
    public Double[] getColumnBy(Object title){
        int c = titles.indexOf(title);
        return this.getColumnAt(c);
    }


    public OData toObjectData(){
        List<Map<String,Object>> data = new ArrayList<>();
        for (String category : body.keySet()) {
            Map<String,Object> map = new HashMap<>();
            map.put("category",category);
            Double[] row = this.getRowBy(category);
            for (int i = 0; i < row.length ; i++) {
                String key = "value"+i;
                Double value = row[i];
                map.put(key,value);
            }
            data.add(map);
        }
        return new OData(name,titles,data);
    }



    public List<Object[]> toPlainData(){
        return toPlainData(this.body,this.titles);
    }

    public static List<Object[]> toPlainData( Map<String, Double[]> body, List titles){
        List<Object[]> list = new ArrayList<>();
        Object[] ts = new Object[titles.size()+1];
        for (int i = 0; i < titles.size(); i++) {
            Object o = titles.get(i);
            ts[i+1] = o;
        }
        list.add(0,ts);
        for (String category : body.keySet()) {
            Double[] ds = body.get(category);
            Object[] os = new Object[ds.length+1];
            os[0] = category;
            for (int i = 0; i < ds.length; i++) {
                os[i+1] = ds[i];
            }
            list.add(os);
        }
        return list;
    }


    public void setColumnAt(int c, Double[] col){
        for (int i = 0; i < col.length; i++) {
            Double value = col[i];
            Double[] row = this.getRowAt(i);
            row[c] = value;
        }
    }

    public void setColumnBy(Object title, Double[] col){
        int c = this.titles.indexOf(title);
        this.setColumnAt(c,col);
    }


    public void setRowAt(int r, Double[] col) throws Exception {
        String category = this.categories.get(r);
        this.setRowBy(category,col);
    }

    public void setRowBy(String category, Double[] row) throws Exception {
        if(categories.indexOf(category)!=-1){
            body.put(category,row);
        }else {
            throw new Exception("没有这个类别项:"+category);
        }


    }


    public void addRowAt(int at, String category, Double[] row){
        categories.add(at,category);
        body.put(category,row);
    }



    public Double[] sum_columns(List titles){
        Double[] sum = new Double[ROW_COUNT];
        for (int i = 0; i < sum.length; i++) {
            sum[i] = 0.0;
        }
        for (Object title : titles) {
            Double[] columnBy = this.getColumnBy(title);
            for (int i = 0; i < ROW_COUNT; i++) {
                Double value = columnBy[i];
                value = value ==null? 0.0:value;
                sum[i] = sum[i]+value;
            }
        }
        return sum;
    }


    public void removeRowBy(String category){
        this.categories.remove(category);
        this.body.remove(category);
    }
    public void removeRowAt(int r){
        String category = this.categories.get(r);
        this.removeRowBy(category);
        this.ROW_COUNT--;
    }

    public VData subRowData(int... rs){
        String[] cats = new String[rs.length];
        for (int i = 0; i < rs.length; i++) {
            cats[i] = categories.get(rs[i]);
        }
        return this.subRowData(cats);
    }
    public VData subRowData(String... cats){
        VData data = new VData();
        Map<String,Double[]> body = new HashMap<>();
        List<String> categories = new ArrayList<>();
        for (int i = 0; i < cats.length; i++) {
            String category = cats[i];
            categories.add(category);
            Double[] values = this.getRowBy(category);
            body.put(category,values);
        }
        data.titles = titles;
        data.categories = categories;
        data.body = body;
        data.ROW_COUNT = categories.size();
        data.COL_COUNT = titles.size();
        return data;
    }


}
