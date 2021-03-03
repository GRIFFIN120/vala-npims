package com.vala.carbon.controllers;

import com.vala.base.controller.BaseController;
import com.vala.carbon.controllers.entity.CarbonFilterEntity;
import com.vala.carbon.service.RegressionService;
import com.vala.commons.bean.KV;
import com.vala.commons.bean.ResponseResult;
import com.vala.commons.bean.data.VData;
import com.vala.framework.data.service.FrameService;
import com.vala.framework.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("carbon-regression")
public class CarbonRegressionController extends BaseController<CarbonFilterEntity> {

    @Autowired
    FrameService frameService;
    @Autowired
    RegressionService regressionService;

    @RequestMapping("/reg")
    public ResponseResult regression(@RequestBody  CarbonFilterEntity filter) throws Exception {

        CarbonFilterEntity filterEntity = this.baseService.get(filter.id);
        // 读取历史因子耦合值Xi
        String xiPath = filter.getXiPath();
        byte[] read = this.fastDfsService.read(xiPath);
        List<Object[]> xi = ExcelUtils.read(new ByteArrayInputStream(read), "xlsx");
        VData Xi = new VData(xi);
        // 设置断点
        String breakPoint = filter.breakPoint;
        int bi = Xi.categories.indexOf(breakPoint);
        bi = bi==-1? 0: bi;
        // 获得回归系数
        int ncomp = filter.ncomp;
        Integer coreId = filterEntity.getFrame().getCore().id;
        Map<String, Double> coreMap =  frameService.getDataValuesById(coreId);
        double[] coefficients = this.regressionService.getCoefficients(Xi, coreMap, bi, ncomp);
        List<KV> coefs = new ArrayList<>();
        for (Object title : Xi.titles) {
            int c = Xi.titles.indexOf(title);
            double coef = coefficients[c];
            KV kv = new KV();
            kv.name=title;
            kv.code = new Double(coef);
            coefs.add(kv);
        }



        String xpPath = filter.getXpPath();
        read = this.fastDfsService.read(xpPath);
        List<Object[]> xp = ExcelUtils.read(new ByteArrayInputStream(read), "xlsx");
        VData Xp = new VData(xp);
        double sum = 0;
        for (int i = 0; i < Xp.titles.size(); i++) {
            Object title =  Xp.titles.get(i);
            Double[] columnBy = Xp.getColumnBy(title);
            double x = columnBy[0];
            double c = coefficients[i];
            sum+= (x*c);
        }

        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("predict",sum);
        ret.put("core",coreMap);
        ret.put("coef",coefs);

        return new ResponseResult(ret);
    }
}
