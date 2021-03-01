package com.vala.carbon.controllers;

import com.vala.commons.bean.ResponseResult;
import com.vala.framework.data.bean.DataFrameTreeBean;
import com.vala.framework.file.controller.FileBaseController;
import com.vala.framework.user.entity.UserBasic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.*;

@RestController
@RequestMapping("/carbon-filter")
public class CarbonFilterController extends FileBaseController<CarbonFilterEntity> {

    @Autowired
    CarbonFilterService service;
    @RequestMapping("/exportResults")
    public ResponseResult exportResults(@RequestBody  CarbonFilterEntity filter) throws Exception {
        List<Object[]> fullData = this.service.getFullData(filter);
        return new ResponseResult(fullData);
    }



    @RequestMapping("/viewResults")
    public ResponseResult viewResults(@RequestBody  CarbonFilterEntity filter) throws Exception {
        List<Object[]> treeData = this.service.getTreeData(filter);
        return new ResponseResult(treeData);
    }


        @RequestMapping("/calculate")
    public ResponseResult calculate(@RequestBody  CarbonFilterEntity filter) throws Exception {

        // 开始计算 （异步操作）
        Integer uid = this.getSession("UID",Integer.class);
        service.doit(filter,uid);

        // 将数据状态更改为"运算中"
        CarbonFilterEntity temp = new CarbonFilterEntity();
        temp.id = filter.id;
        temp.setState("calculating");
        temp.setExpired(null);
        temp.setDescription("请等待...");
        this.baseService.saveOrUpdate(temp);

        return new ResponseResult(200,"后台运算中，完成运算后可查看结果。",temp);
    }

    @Transactional
    @Override
    public void beforeOutput(CarbonFilterEntity bean) {
        Integer frameId = bean.getFrame().getId();
        DataFrameTreeBean tree = new DataFrameTreeBean();
        tree.setFrameId(frameId);
        tree.setNodeType(2);
        List<DataFrameTreeBean> all = this.baseService.getRepo().findAll(Example.of(tree));
        boolean isAdjuest = true;
        boolean isPredict =true;
        for (DataFrameTreeBean data : all) {
            if(!isAdjuest || !isPredict) break;
            Double parameter = data.getParameter();
            Double predict = data.getPredict();
            isAdjuest = parameter != null;
            isPredict = predict!=null;
        }
        bean.isAdjustmentAssigned = isAdjuest;
        bean.isPredictionAssigned = isPredict;
    }

    @Override
    public void beforeInsert(CarbonFilterEntity ext) {
        ext.setState("prepare");
        Integer uid = this.getSession("UID", Integer.class);
        UserBasic userBasic = this.baseService.get(UserBasic.class, uid);
        ext.setUser(userBasic);
    }


}
