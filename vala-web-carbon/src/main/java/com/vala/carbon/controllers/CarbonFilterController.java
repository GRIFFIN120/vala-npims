package com.vala.carbon.controllers;

import cn.hutool.core.util.ArrayUtil;
import com.vala.base.utils.TreeUtils;
import com.vala.commons.bean.ResponseResult;
import com.vala.commons.bean.data.OData;
import com.vala.commons.bean.data.VData;
import com.vala.framework.data.bean.DataBean;
import com.vala.framework.data.bean.DataFrameBean;
import com.vala.framework.data.bean.DataFrameTreeBean;
import com.vala.framework.data.service.FrameService;
import com.vala.framework.file.controller.FileBaseController;
import com.vala.framework.user.entity.UserBasic;
import com.vala.framework.utils.ExcelUtils;
import com.vala.service.RService;
import org.rosuda.REngine.Rserve.RConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.*;

@RestController
@RequestMapping("/carbon-filter")
public class CarbonFilterController extends FileBaseController<CarbonFilterEntity> {

    @Autowired
    FrameService frameService;

    @Autowired
    FilterService filterService;

    @Autowired
    ForestService forestService;

    @Autowired
    ForecastService forecastService;

    @Transactional
    @RequestMapping("/X")
    public ResponseResult X(@RequestBody  CarbonFilterEntity filter) throws Exception {
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







    @RequestMapping("/exportResults")
    public ResponseResult exportResults(@RequestBody  CarbonFilterEntity filter) throws Exception {
        CarbonFilterEntity filterEntity = this.baseService.get(filter.id);
        // 获取文件数据
        String fieldName = "url";
        VData vdata = this.filterService.getDataFromFileField(filterEntity, fieldName);
        // 重做表头
        Map<Integer, DataBean> mapping = this.frameService.dataMapping(filterEntity.frame);
        List<String> nTitles = new ArrayList<>();
        for (Object temp : vdata.getTitles()) {
            Integer dataId = (Integer) temp;
            DataBean dataBean = mapping.get(dataId);
            nTitles.add(dataBean.getName());
        }
        vdata.setTitles(nTitles);
        // 输出数据
        List<Object[]> plain = vdata.toPlainData();
        return new ResponseResult(plain);
    }


    @RequestMapping("/viewResults")
    public ResponseResult viewResults(@RequestBody  CarbonFilterEntity filter) throws Exception {
        CarbonFilterEntity filterEntity = this.baseService.get(filter.id);
        // 获取文件数据
        String fieldName = "url";
        VData vdata = this.filterService.getDataFromFileField(filterEntity, fieldName);
        // 获取树形列表，并组装数据
        List<DataFrameTreeBean> treeList = this.filterService.getTreeList(filter);
        this.filterService.assembleTreeList(treeList,vdata);

        // 合成树
        List<DataFrameTreeBean> tree = TreeUtils.treeBean(treeList);
        tree.sort(Comparator.comparing(DataFrameTreeBean::getTimestamp).reversed());
        this.filterService.sumTreeData(tree, vdata.ROW_COUNT, true);
        // 输出树
        VData tData = filterService.tree2data(tree,vdata.getCategories());
        return new ResponseResult(tData.toObjectData());
    }




    @RequestMapping("/calculate")
    public ResponseResult calculate(@RequestBody  CarbonFilterEntity filter) throws Exception {
        CarbonFilterEntity filterEntity = this.baseService.get(filter.id);

        // 获取数据并做归一化
        DataFrameBean frame = filterEntity.getFrame();
        VData data = frameService.getData(frame,"id",true);
        data.unify(null);

        // 异步执行运算
        Integer filterId = filter.id;
        Integer window = filter.win;
        String name = filter.getName();
        Integer uid = this.getSession("UID",Integer.class);
        forestService.handler(data,window,name,filterId,uid);

        // 更新状态
        filterEntity.setState("calculating");
        filterEntity.setExpired(null);
        filterEntity.setDescription("请等待...");
        this.baseService.saveOrUpdate(filterEntity);

        return new ResponseResult(200,"后台运算中，完成运算后可查看结果。",filterEntity);
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
