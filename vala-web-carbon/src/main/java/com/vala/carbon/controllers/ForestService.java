package com.vala.carbon.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vala.base.entity.BaseEntity;
import com.vala.base.service.BaseService;
import com.vala.base.service.FastDfsService;
import com.vala.commons.bean.data.VData;
import com.vala.framework.utils.ExcelUtils;
import com.vala.service.RService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ForestService {

    @Autowired
    private SimpMessagingTemplate messageTemplate;

    @Resource
    private BaseService<CarbonFilterEntity> baseService;

    @Autowired
    public FastDfsService fastDfsService;

    @Async
    public void handler(VData vdata, int window, String name,Integer filterId, Integer uid) throws Exception {
        // 计算
        long startTime = System.currentTimeMillis();
        Map<String, Double[]> forest = this.forest(vdata, window);
        long endTime = System.currentTimeMillis();
        int time = (int) (endTime-startTime)/1000;
        List titles = vdata.titles;
        titles.remove(0);
        List<Object[]> objects = VData.toPlainData(forest, titles);


        // 写入文件
        byte[] bytes = ExcelUtils.write(name, objects);
        String url = fastDfsService.upload(bytes, "xlsx");
        // 更新状态
        this.updateState(filterId, url, time);
        this.sendMsg(filterId,uid);
    }

    public void sendMsg(Integer filterId, Integer uid) throws JsonProcessingException {
        BaseEntity baseEntity = new BaseEntity();
        baseEntity.id = filterId;
        baseEntity.code="true";
        if(uid!=null){
            ObjectMapper mapper = new ObjectMapper();
            String s = mapper.writeValueAsString(baseEntity);
            messageTemplate.convertAndSendToUser(uid.toString(), "/queue/points", s);
        }
    }

    public void updateState(Integer filterId, String url, int time){
        CarbonFilterEntity temp = this.baseService.get(CarbonFilterEntity.class,filterId);
        temp.setState("calculated");
        temp.setExpired(false);
        temp.description = "运算耗时"+time+"秒";
        // 删除旧文件，保存新文件地址
        if(temp.url!=null){
            this.fastDfsService.delete(temp.url);
        }
        temp.url = url;
        // 更新数据库
        this.baseService.getRepo().save(temp);
    }

    public Map<String, Double[]> forest(VData vdata, int window) throws Exception {
        Map<String, double[][]> map = this.convert2WindowData(vdata, window);
        Map<String, Double[]> calculation = this.calculation(map);
        return calculation;
    }

    private Map<String, Double[]> calculation(Map<String, double[][]> map) throws Exception {
        Map<String, Double[]> result = new LinkedHashMap<>();
        RService rService = new RService();
        for (String category : map.keySet()) {
            double[][] data = map.get(category);// m: 指标数， n: 日期数
            Double[] forest = rService.forest(data);
            result.put(category,forest);
        }
        rService.close();
        return result;
    }

    private  Map<String, double[][]>  convert2WindowData(VData data, Integer window ){
        List<String> categories = data.getCategories();
        Map<String, double[][]> windowMap = new LinkedHashMap<>();
        for (int i = window-1; i < data.ROW_COUNT; i++) {
            String category = categories.get(i);
            double[][] sub = new double[data.COL_COUNT][window];
            for (int j = 0; j < window; j++) {
                String cat =  categories.get(i-window+j+1);
                Double[] value = data.getRowBy(cat);
                for (int k = 0; k < data.COL_COUNT; k++) {
                    double d = value[k];
                    sub[k][j] = d;
                }
            }
            windowMap.put(category,sub);
        }
        return windowMap;
    }

}
