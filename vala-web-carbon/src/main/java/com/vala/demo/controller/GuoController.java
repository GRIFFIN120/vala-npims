package com.vala.demo.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.vala.base.controller.BaseController;
import com.vala.framework.utils.ExcelUtils;
import net.sf.json.JSONArray;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Query;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/guo")
public class GuoController extends BaseController<GuoEntity> {

    String root = "/Users/liuyinpeng/Documents/2月/疫苗";
    int[] dues = new int[]{3,6,9,12};
    int[] valids = new int[]{60,70,80,90};
    int[] rates = new int[]{8,16,24,32,40,48,56,64,72,80};
    String s0 = "select round(value) from guo_entity where due=%s and valid=%s and rate = %s order by span";
    String s1 = "select  min(span) from guo_entity where due=%s and valid=%s and rate = %s and immune=1";
    String n = "/results/%s_%s_%s_%s.txt";


    @RequestMapping("parse")
    public List<GuoEntity> parse() throws Exception {
        List<GuoEntity> res = new ArrayList<>();
        for (int rate : rates) {
            for (int valid : valids) {
                for (int due : dues) {
                    String sql0 = String.format(s0,due,valid,rate);
                    String sql1 = String.format(s1,due,valid,rate);
                    Query nativeQuery = this.baseService.getEntityManager().createNativeQuery(sql0);
                    List data = nativeQuery.getResultList();
                    nativeQuery = this.baseService.getEntityManager().createNativeQuery(sql1);
                    Object temp = nativeQuery.getSingleResult();
                    Integer time = temp==null ? null: (int) temp;
                    GuoEntity guo = new GuoEntity();
                    guo.data = data;
                    guo.time = time;
                    guo.rate = rate;
                    guo.valid = valid;
                    guo.due = due;
                    guo.valueText = temp==null ? ">0024月": time+"个月";
                    res.add(guo);
                }
            }
        }
        return res;
    }

    @RequestMapping("load")
    public String load() throws Exception {


        for (int rate : rates) {
            for (int valid : valids) {
                for (int due : dues) {
                    String sql0 = String.format(s0,due,valid,rate);
                    String sql1 = String.format(s1,due,valid,rate);
                    Query nativeQuery = this.baseService.getEntityManager().createNativeQuery(sql0);
                    List list = nativeQuery.getResultList();
                    String str = CollectionUtil.join(list,",");
                    nativeQuery = this.baseService.getEntityManager().createNativeQuery(sql1);
                    Object time = nativeQuery.getSingleResult();

                    String name = root+String.format(n,rate,valid,due,time==null?0:time);
                    FileUtils.write(new File(name),str,"utf-8");
                }
            }
        }


        return "123";
    }


    @RequestMapping("init")
    public List<GuoEntity> guo() throws Exception {
        Double para = (3.11-1)*10000/3.11;
        List<GuoEntity> list = new ArrayList<>();
        String path = root+ "/副本模型结果(2)(1).xlsx";
        FileInputStream fis = new FileInputStream(new File(path));
        Workbook book = ExcelUtils.getWorkBook(fis, "xlsx");
        int rows = 26;
        int cols = 17;
        for (Sheet sheet : book) {
            Integer rate = Integer.parseInt(sheet.getSheetName());
            for (int c = 1; c < cols; c++) {
                Integer due = (int) sheet.getRow(0).getCell(c).getNumericCellValue();
                Integer valid = (int) (sheet.getRow(1).getCell(c).getNumericCellValue()*100);
                for (int r = 2; r < rows; r++) {
                    Integer span = r-1;
                    Double value = sheet.getRow(r).getCell(c).getNumericCellValue();
                    Boolean immune = value>=para ? true : false;
                    GuoEntity item = new GuoEntity();
                    item.due = due;
                    item.valid = valid;
                    item.span = span;
                    item.rate = rate;
                    item.value = value;
                    item.immune = immune;
                    list.add(item);
                }
            }
        }
        this.baseService.getRepo().saveAll(list);
        return list;
    }
}
