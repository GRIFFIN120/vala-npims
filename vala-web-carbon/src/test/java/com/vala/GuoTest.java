package com.vala;

import com.vala.framework.utils.ExcelUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class GuoTest {

    public Double para = (3.11-1)*10000/3.11;

    @Test
    public void test() throws Exception {
        String path = "/Users/liuyinpeng/Documents/2月/一秒/副本模型结果(2)(1).xlsx";
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
                    Item item = new Item(due,valid,span,rate,value, immune);
                    item.write();
                }
            }
        }
    }
}

class Item {
    public Integer due;
    public Integer valid;
    public Integer span;
    public Integer rate;
    public Double value;
    public Boolean immune;
    public void write(){
        System.out.println(due+","+valid+","+span+","+rate+","+value+","+immune);
    }

    public Item(Integer due, Integer valid, Integer span, Integer rate, Double value, Boolean immune) {
        this.due = due;
        this.valid = valid;
        this.span = span;
        this.rate = rate;
        this.value = value;
        this.immune = immune;
    }
}
