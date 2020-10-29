package com.vala.jsoup.example;

import com.vala.base.bean.SearchBean;
import com.vala.base.bean.SearchResult;
import com.vala.base.service.BaseServiceImpl;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import javax.persistence.Query;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CncpService extends BaseServiceImpl<Cncp> {

    static String url = "http://k.tanjiaoyi.com:8080/KDataController/getHouseDatasInAverage.do?jsoncallback=jQuery111205751557890688976_1551878977302&lcnK=f57f50a55dc99564468dba987810aaff&brand=TAN&_=1551878977303";
    static String[] houses = new String[] {"北京","上海","湖北","重庆","广东","天津","深圳","福建"};
    static String[] housesEN = new String[] {"Beijing","Shanghai","Hubei","Chongqing","Guangdong","Tianjin","Shenzhen","Fujian"};
    static int[]    houseOffset = new int[] {1,1,1,1,1,1,6,1};


    public void stat(){
        String sql = "select name, ROUND(avg(deal),2) x ,ROUND(sum(num)/1000000,2) y, ROUND(sum(amount)/1000000,2) z from CNCP group by name";
        Query nativeQuery = this.getEntityManager().createNativeQuery(sql);
        List<Object[]> dateList = nativeQuery.getResultList();

        Map<String, Object[]> map = new LinkedHashMap<>();
        for (String name : houses) {
            map.put(name,null);
        }


        for (Object[] objects : dateList) {
            String name = (String) objects[0];
            map.put(name,objects);
//            String v1 = (String) objects[1];
//            Double v2 = (Double) objects[2];
//            Double v3 = (Double) objects[3];
        }

        for (String key : map.keySet()) {
            String line = StringUtils.join(map.get(key),",");
            System.out.println(line);
        }

    }



    public List<Object[]> getPrice(){

        Map<String, Object[]> map = new LinkedHashMap<>();

        List<Object[]> list = new ArrayList<>();

        Query nativeQuery = this.getEntityManager().createNativeQuery("SELECT distinct(date) k FROM Cncp order by date asc");
        List<String> dateList = nativeQuery.getResultList();
        for (String date : dateList) {
            Object[] data = new Object[houses.length+1];
            data[0] = date;
            map.put(date, data);
        }
        Object[] title = new Object[houses.length+1];
        for (int i = 1; i < title.length; i++) {
            title[i] = housesEN[i-1];
            String name = houses[i-1];
            Query query = this.getEntityManager().createNativeQuery("select date, deal from Cncp where name = ? order by date");
            query.setParameter(1,name);
            List<Object[]> result = query.getResultList();
            for (int j = 0; j < result.size(); j++) {
                Object[] objects =  result.get(j);
                String date = (String) objects[0];
                Double deal = (Double) objects[1];

                Object[] array = map.get(date);

                array[i] = deal;
            }
        }
        list.add(title);
        for (String date : map.keySet()) {
            list.add(map.get(date));
        }

        return list;

    }






    public void update() throws Exception {
        Map<String, String> map = new HashMap<>();

        Query nativeQuery = this.getEntityManager().createNativeQuery("SELECT name ,max(date) k FROM Cncp group by name");
        List<Object[]> resultList = nativeQuery.getResultList();
        for (Object[] o : resultList) {
            String house = (String) o[0];
            String date = (String) o[1];
            System.out.println(house + " " +date);
            map.put(house,date);
        }

        List<Cncp> load = download(map);
        for (int i = 0; i < load.size(); i++) {
            Cncp cncp =  load.get(i);
            System.out.println(i + "/" + load.size() + " " +cncp);
            this.saveOrUpdate(cncp);
        }
    }





    public List<Cncp> download(Map<String, String> map) throws Exception {
        List<Cncp> list = new ArrayList<>();
        Document d = Jsoup.connect(url).header("Accept-Encoding", "gzip, deflate")
                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0").maxBodySize(0)
                .timeout(300000).post();
        String txt = d.text();
        int start = txt.indexOf("(")+1;
        int end = txt.lastIndexOf(")");
        txt = txt.substring(start,end);
        JSONObject jo = JSONObject.fromObject(txt);

        for (int i = 0; i < houses.length; i++) {
            String name =  houses[i];
            JSONArray ja = jo.getJSONArray(name);
            String lastDate = map.get(name);
            for (int j = 0; j < ja.size(); j++) {
                JSONObject jsonObject = (JSONObject)ja.get(j);
                String date = jsonObject.getString("INDATE");
                if(compare(date,lastDate)) {
                    Double deal = jsonObject.getDouble("deal")/houseOffset[i];/////DEAL
                    Double amount = jsonObject.getDouble("DEALAMOUNT");////////////////DEALAMOUNT
                    Double num = jsonObject.getDouble("DEALNUM");//////////////////////DEALNUM
                    deal = (double) (Math.round(deal*100))/100;
                    amount = (double) (Math.round(amount*100))/100;
                    num = (double) (Math.round(num*100))/100;
                    Cncp cp = new Cncp(name, date, deal, amount, num, this.getDate());
                    list.add(cp);
                }
            }

        }
        return list;
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


    private boolean compare(String curr, String last) throws Exception {
        Date d1 = sdf.parse(curr);
        Date d2 = sdf.parse(last);
        return (d1.getTime() > d2.getTime());
    }

    private String getDate(){
        return sdf.format(new Date());
    }

    @Override
    public SearchResult<Cncp> search(SearchBean<Cncp> search) {
        return null;
    }

}
