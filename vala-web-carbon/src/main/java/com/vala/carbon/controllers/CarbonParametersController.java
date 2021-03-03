package com.vala.carbon.controllers;

import com.vala.base.controller.BaseController;
import com.vala.carbon.controllers.entity.CarbonFilterEntity;
import com.vala.carbon.controllers.entity.CarbonParameters;
import com.vala.commons.bean.ResponseResult;
import com.vala.framework.utils.ExcelUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/carbon-param")
public class CarbonParametersController extends BaseController<CarbonParameters> {

    @RequestMapping("/uploadParams/{filterId}")
    public ResponseResult uploadParams(@RequestBody List<Object[]> list, @PathVariable Integer filterId) throws Exception {
        CarbonFilterEntity filterEntity = this.baseService.get(CarbonFilterEntity.class, filterId);
        String name = filterEntity.name+"的控制参数";
        byte[] write = ExcelUtils.write(name, list);
        String paramPath = this.fastDfsService.upload(write, "xlsx");
        filterEntity.setParamPath(paramPath);
        this.baseService.getRepo().save(filterEntity);
        return new ResponseResult("上传成功");
    }

}
