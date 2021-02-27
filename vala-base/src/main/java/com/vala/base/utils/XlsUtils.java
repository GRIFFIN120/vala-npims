package com.vala.base.utils;

import com.vala.base.entity.BaseEntity;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class XlsUtils {

    private static String[] getTitles(Sheet sheet ){
        Row title = sheet.getRow(0);
        String[] titles = new String[title.getLastCellNum()];
        for (int j = 0; j < title.getLastCellNum(); j++) {
            Cell cell = title.getCell(j);
            String content = cell.toString().trim();
            titles[j] = content;
        }
        return titles;
    }

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

    public static List<String[]> read(File file, boolean hasTitle) throws Exception {
        List<String[]> list = new ArrayList<>();
        String fileName = file.getName();

        System.out.println(fileName);

        String[] split = fileName.split("[.]");

        Workbook book;
        if ( "xls".equals(split[1])){
            book = new HSSFWorkbook(new FileInputStream(file));
        }else if ("xlsx".equals(split[1])){
            book = new XSSFWorkbook(new FileInputStream(file));
        }else {
            throw new Exception("文件类型错误!");
        }


        Sheet sheet = book.getSheetAt(0);
        String sheetName = sheet.getSheetName();
        int r = sheet.getLastRowNum();
        int c = maxColumnSize(sheet);
        int start = hasTitle?1:0;
        for (int i= start; i <= r;i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                String[] ar = new String[c];
                for (int j = 0; j < c; j++) {
                    Cell cell = row.getCell(j);
                    if(cell!=null){
                        String content = getValue(cell);
                        ar[j] = content;
                    }
                }
                list.add(ar);
            }
        }

        return list;
    }


    private static int maxColumnSize(Sheet sheet){
        int max = 0;

        for (int i= 0; i <= sheet.getLastRowNum();i++) {
            Row row = sheet.getRow(i);
            int c = row.getLastCellNum();
            if(c>max) max = c;
        }
        return max;
    }


    private  static String getValue(Cell hssfCell) {

        if (hssfCell.getCellType()== HSSFCell.CELL_TYPE_NUMERIC&&HSSFDateUtil.isCellDateFormatted(hssfCell)) {
            Date date = hssfCell.getDateCellValue();
            return sdf.format(date);
        } else if (hssfCell.getCellType() == hssfCell.CELL_TYPE_BOOLEAN) {
            // 返回布尔类型的值
            return String.valueOf(hssfCell.getBooleanCellValue());
        } else if (hssfCell.getCellType() == hssfCell.CELL_TYPE_NUMERIC) {
            // 返回数值类型的值
            Object inputValue = null;// 单元格值
            Long longVal = Math.round(hssfCell.getNumericCellValue());
            Double doubleVal = hssfCell.getNumericCellValue();
            if(Double.parseDouble(longVal + ".0") == doubleVal){   //判断是否含有小数位.0
                inputValue = longVal;
            }
            else{
                inputValue = doubleVal;
            }
            DecimalFormat df = new DecimalFormat("#.####");    //格式化为四位小数，按自己需求选择；
            return String.valueOf(df.format(inputValue));      //返回String类型
        } else {
            // 返回字符串类型的值
            return String.valueOf(hssfCell.getStringCellValue());
        }
    }


    public static <T extends BaseEntity> List<T> read(MultipartFile file, Class<T> domain)  throws Exception {

        List<T> list = new ArrayList<>();


        String fileName = file.getOriginalFilename();
        String[] split = fileName.split("\\.");
        String fix = split[split.length-1];
        Workbook book;
        if ( "xls".equals(split[1])){
            book = new HSSFWorkbook(file.getInputStream());
        }else if ("xlsx".equals(split[1])){
            book = new XSSFWorkbook(file.getInputStream());
        }else {
            throw new Exception("文件类型错误!");
        }
        Sheet sheet = book.getSheetAt(0);
        String[] titles = getTitles(sheet);
        for (int i= 1; i <= sheet.getLastRowNum();i++) {
            Row row = sheet.getRow(i);
            T bean =  domain.newInstance();
            for (int j = 0; j < titles.length; j++) {
                String title = titles[j];
                Cell cell = row.getCell(j);
                String content = null;
                if (cell.getCellType()== HSSFCell.CELL_TYPE_NUMERIC&&HSSFDateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    content = sdf.format(date);
                }else {
                    content = cell.toString();
                }



                Field field = domain.getField(title);
                if(field.getType().equals(String.class)){
                    field.set(bean,content);
                }else{
                    Object valueOf = Class.forName(field.getType().getTypeName()).getMethod("valueOf",String.class).invoke(null, content);
                    field.set(bean,valueOf);
                }
            }
            list.add(bean);
        }
        return list;
    }


    public static void read(String fileName,InputStream inputStream, XlsLineReader reader) throws Exception {

        String[] split = fileName.split("\\.");
        String fix = split[split.length-1];
        Workbook book;
        if ( "xls".equals(split[1])){
            book = new HSSFWorkbook(inputStream);
        }else if ("xlsx".equals(split[1])){
            book = new XSSFWorkbook(inputStream);
        }else {
            throw new Exception("文件类型错误!");
        }

        Sheet sheet = book.getSheetAt(0);
        String sheetName = sheet.getSheetName();
        int r = sheet.getLastRowNum();
        Row title = sheet.getRow(0);
        String[] titles = new String[title.getLastCellNum()];
        for (int j = 0; j < title.getLastCellNum(); j++) {
            Cell cell = title.getCell(j);
            String content = cell.toString().trim();
            titles[j] = content;
        }
        String checkResult = reader.checkTitle(titles);
        if(null!=checkResult){
            throw new Exception(checkResult);
        }

        int start = reader.getStart();
        for (int i= start; i <= r;i++) {
            Row row = sheet.getRow(i);
            Map<Object,Object> map = new HashMap<>();
            if (row == null) continue;;
            for (int j = 0; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                String content = cell.toString();
                map.put(j,content);
            }
            reader.setCurrentRow(i);
            reader.readLine(fileName,sheetName,map);
        }


    }


    private static String encodingFileName(String fileName, HttpServletRequest request) throws Exception {
        String agent = request.getHeader("USER-AGENT");
        if (agent.contains("MSIE") || agent.contains("Trident")) {
            return java.net.URLEncoder.encode(fileName, "UTF-8");
        } else {
            return new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
        }
    }

    public static void outExcel(String fileName, List<String> titles, List<Object[]> list, HttpServletRequest request, HttpServletResponse response) {
        String[] atitles = new String[titles.size()];
        atitles = titles.toArray(atitles);
        outExcel(fileName,atitles,list,request,response);
    }



    public static void outExcel(String fileName, String[] titles, List<Object[]> list, HttpServletRequest request, HttpServletResponse response) {
        OutputStream os = null;
        XSSFWorkbook workbook=new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        try {
            response.reset();
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/vnd.ms-excel");
            fileName = encodingFileName(fileName, request);
            response.addHeader("Content-Disposition", "attachment;filename=" + fileName+".xlsx");
            os = response.getOutputStream();
            XSSFRow titleRow = sheet.createRow(0);
            for (int i = 0; i < titles.length; i++) {
                String string = titles[i];
                titleRow.createCell(i).setCellValue(string);
            }
            for (int i = 0; i < list.size(); i++) {
                XSSFRow row = sheet.createRow(i+1);
                Object[] objects =  list.get(i);
                for (int j = 0; j < objects.length; j++) {
                    Object object =  objects[j];
                    String str = object == null ? "" : object.toString();
                    try {
                        Double d = Double.parseDouble(str);
                        row.createCell(j).setCellValue(d);
                    } catch (Exception e) {
                        row.createCell(j).setCellValue(str);
                    }
                }
            }
            workbook.write(os);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
