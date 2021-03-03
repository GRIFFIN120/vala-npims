package com.vala.demo.controller;

import cn.hutool.core.util.ArrayUtil;
import com.vala.base.controller.BaseController;
import com.vala.base.service.BaseService;
import com.vala.eutl.Cmpliance;
import com.vala.framework.user.entity.UserBasic;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/eutl")
public class EutlController extends BaseController<Tr> {
    private void upFreq(Integer id, Integer freq){
        String s = "update tr set freq = ? where id = ?";
        Query nativeQuery = this.baseService.getEntityManager().createNativeQuery(s);
        nativeQuery.setParameter(1,freq);
        nativeQuery.setParameter(2,id);
        nativeQuery.executeUpdate();
    }

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

//    @RequestMapping("/span")
//    public void span(){
//        String SQL = "select phase,date from tr  group by phase,date order by phase,date";
//
//    }



    @RequestMapping("/freq")
    public void freq() throws Exception {
        this.freqdo(1);
//        this.freqdo(2);
    }


    public void freqdo(int phase) throws Exception {
        List<String> hids = this.hids(phase);

        int c = 1 ;
        for (String hid : hids) {
            System.out.println( c++ +"/" + hids.size() +"  :  "+hid  +" freq  phase-"+phase);
            Query nativeQuery = this.baseService.getEntityManager().createNativeQuery("select id,tdate from tr where hid = ? and phase=? order by tdate");
            nativeQuery.setParameter(1,hid);
            nativeQuery.setParameter(2,phase);
            List<Object[]> li = nativeQuery.getResultList();
            if(li.size()==0) continue;;
            for (Object[] objects : li) {
                System.out.println(ArrayUtil.join(objects,","));

            }



//            upFreq((Integer) li.get(0)[0],0);
//
            for (int i = 1; i < li.size(); i++) {
                Object[] o0 =  li.get(i-1); // 上次
                Object[] o1 =  li.get(i);   // 本次

                Date d0 = sdf.parse((String) o0[1]);
                Date d1 = sdf.parse((String) o1[1]);

                Long daysBetween =(d1.getTime()-d0.getTime()+1000000)/(60*60*24*1000);

                Integer freq = daysBetween.intValue();


                Integer id = (Integer) o1[0];

                System.out.println(id+" "+freq+"  "+o0[1]+" "+o1[1]);

//                upFreq(id,freq);

            }




        }

    }




    public Map<Integer,Cmpliance> yearlyCompliance(String hid, int phase){

        Map<Integer,Cmpliance> map = new LinkedHashMap<>();

        String SQL = "select \n" +
                "year, allo, surr , gap, @i\\:=@i+gap pos\n" +
                "from \n" +
                "(select a.year,if(b.allo is null,0,b.allo) allo, if(b.surr is null,0,b.surr) surr,if(b.allo is null,0,b.allo) -  if(b.surr is null,0,b.surr) gap " +
                "from (select year from comp where phase="+phase+" order by year asc) a left join " +
                "(select year,allo,surr from  tcompliance where hid =? and phase = "+phase+" ) b on a.year = b.year) t1\n" +
                ",(select @i\\:=0) t2";

        Query nativeQuery = this.baseService.getEntityManager().createNativeQuery(SQL);
        nativeQuery.setParameter(1,hid);
        List<Object[]> list = nativeQuery.getResultList();
        int max = 0;

        for (Object[] objects : list) {

            Cmpliance c = new Cmpliance();

            Integer year = (Integer) objects[0];

            max = year>max? year: max;

            Number allo = (Number) objects[1];
            Number surr = (Number) objects[2];
            Number gap = (Number) objects[3];
            Number pos = (Number) objects[4];

            c.setYear(year);
            c.setAllo(allo.intValue());
            c.setSurr(surr.intValue());
            c.setGap(gap.intValue());
            c.setPosi(pos.intValue());

            map.put(year,c);
        }
        return map;
    }

    @Transactional
    public void _init(String hid, int phase) throws Exception {
        Map<Integer, Cmpliance> cmap = yearlyCompliance(hid, phase);
        String SQL = "select tdate,size,aft-size bef, aft,id from (select id,tdate, size, @i\\:=@i+size as aft   from (select id,tdate,size from tr where phase = ? and hid = ? order by tdate) a, (select @i\\:=0) b) t";
        List<Tr> res = new ArrayList<>();
        Query nativeQuery = this.baseService.getEntityManager().createNativeQuery(SQL);
        nativeQuery.setParameter(1,phase);
        nativeQuery.setParameter(2,hid);
        List<Object[]> list = nativeQuery.getResultList();
        for (Object[] objects : list) {
            String date = (String) objects[0];
            Number size = (Number) objects[1];
            Number before = (Number) objects[2];
            Number after = (Number) objects[3];
            Integer id = (Integer) objects[4];

            Tr tr = this.baseService.get(id);
            tr.setBef(before.intValue());
            tr.setAft(after.intValue());
            int year = Integer.parseInt(date.substring(0, 4));
            Cmpliance c = cmap.get(year);
            if(c!=null){
                tr.setAllo(c.getAllo());
                tr.setSurr(c.getSurr());
                tr.setGap(c.getGap());
                tr.setPos(c.getPosi());
            }else {
                tr.setAllo(0);
                tr.setSurr(0);
                tr.setGap(0);
                tr.setPos(0);
            }
            this.baseService.getRepo().save(tr);
        }
    }



    public void init() throws Exception {
        int phase = 2;
        List all = this.baseService.getRepo().findAll(Example.of(new Tr()));
        List<String> hids = this.hids(phase);
        int i = 1;
        for (String hid : hids) {
            this._init(hid,phase);
            System.out.println(hid+" "+i+++"/"+hids.size());
        }
    }


    public List<String> hids(int phase){
        Query nativeQuery = this.baseService.getEntityManager().createNativeQuery("select distinct(hid) from tholder where phase = "+phase);
        List<String> resultList = nativeQuery.getResultList();
        return resultList;
    }


    public void createDailyTrade(){
        Integer phase = 2;
        String sql = "select  tdate, sum(v) from (select tdate, if(htr=?,-1,0)+if(hac=?,1,0)*volume v from trade where (htr=? or hac = ?) and phase = "+phase+") t group by tdate order by tdate";
        Query nativeQuery = this.baseService.getEntityManager().createNativeQuery("select distinct(hid) from tholder where phase = "+phase);
        List resultList = nativeQuery.getResultList();
        int i = 1;
        for (Object o : resultList) {
            String hid = (String) o;
            nativeQuery = this.baseService.getEntityManager().createNativeQuery(sql);
            nativeQuery.setParameter(1,hid);
            nativeQuery.setParameter(2,hid);
            nativeQuery.setParameter(3,hid);
            nativeQuery.setParameter(4,hid);
            List<Object[]> resultList1 = nativeQuery.getResultList();
            List<Tr> list = new ArrayList<>();
            for (Object[] objects : resultList1) {
                Tr t = new Tr();
                t.hid = hid;
                t.phase = phase;
                t.tdate = (String) objects[0];
                t.size = (Double) objects[1];
                list.add(t);
            }
            this.baseService.getRepo().saveAll(list);
            System.out.println(i+++"/"+resultList.size());
        }
    }



}
