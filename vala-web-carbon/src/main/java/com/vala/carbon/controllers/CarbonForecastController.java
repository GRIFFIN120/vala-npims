package com.vala.carbon.controllers;

import com.vala.carbon.controllers.entity.CarbonFilterEntity;
import com.vala.carbon.service.ForecastService;
import com.vala.commons.bean.ResponseResult;
import com.vala.commons.bean.data.OData;
import com.vala.commons.bean.data.VData;
import com.vala.framework.file.controller.FileBaseController;
import com.vala.framework.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.List;

@RestController
@RequestMapping("carbon-forecast")
public class CarbonForecastController extends FileBaseController<CarbonFilterEntity> {

    @Autowired
    ForecastService forecastService;

    @Transactional
    @RequestMapping("/X")
    public ResponseResult X(@RequestBody CarbonFilterEntity filter) throws Exception {
        Integer uid = this.getSession("UID",Integer.class);
        forecastService.handler(filter.id,uid);
        return new ResponseResult("运算中，请稍候... ");
    }

    @Transactional
    @RequestMapping("/getCombine/{fieldName}")
    public ResponseResult getCombine(@RequestBody  CarbonFilterEntity filter, @PathVariable String fieldName) throws Exception {
        Field field =  CarbonFilterEntity.class.getField(fieldName);
        String url = (String) field.get(filter);
        byte[] read = this.fastDfsService.read(url);
        List<Object[]> xlsx = ExcelUtils.read(new ByteArrayInputStream(read), "xlsx");
        VData data = new VData(xlsx);
        OData oData = data.toObjectData();
        return new ResponseResult(oData);
    }



}
