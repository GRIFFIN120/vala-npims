package com.vala.base.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public abstract class XlsLineReader {
    Integer start = 0;
    Integer currentRow;
    List resluts = new ArrayList();
    String[] titles ;

    public void setTitles(String... titles) {
        this.titles = titles;
    }

    public XlsLineReader(Integer start) {
        this.start = start;
    }


    public String checkTitle(String[] contents){
        if(titles==null||titles.length==0) return null;
        if(contents.length<titles.length) return "列数不够";
        for (int i = 0 ; i < titles.length;i++){
            String title = titles[i];
            String content = contents[i];
            if(title==null) continue;
            if(!title.equals(content)){
                return "文件第["+(i+1)+"]列应为["+title+"]";
            }
        }
        return null;
    }


    public abstract void readLine(String fileName, String sheetName, Map<Object,Object> map) throws Exception;
}
