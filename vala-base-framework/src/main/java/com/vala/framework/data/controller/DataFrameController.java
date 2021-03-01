package com.vala.framework.data.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.vala.base.controller.BaseController;
import com.vala.base.entity.BaseEntity;
import com.vala.commons.bean.ResponseResult;
import com.vala.commons.util.Constants;
import com.vala.framework.data.bean.*;
import com.vala.framework.menu.entity.MenuItem;
import com.vala.framework.user.entity.UserBasic;
import com.vala.framework.utils.ExcelUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/data-frame")
public class DataFrameController extends BaseController<DataFrameBean> {

    @Override
    public void beforeInsert(DataFrameBean ext) {
        Integer uid = this.getSession("UID", Integer.class);
        UserBasic userBasic = this.baseService.get(UserBasic.class, uid);
        System.out.println(userBasic);
        ext.setUser(userBasic);
    }

    private void updateDataTree(DataFrameBean ext){
        Integer frameId = ext.getId();
        List<Integer> current = this.getCurrent(ext);
        Map<Integer,Integer> treed = this.getTreed(frameId);
        List<Integer> insert = new ArrayList<>(); // 要新增的
        List<Integer> delete = new ArrayList<>(); // 要删除的
        List<Integer> temp = new ArrayList<>();
        for (Integer treeId : treed.keySet()) {
            Integer idx = current.indexOf(treeId);
            if(idx==-1){
                delete.add(treeId);
            }else{
                temp.add(treeId);
            }
        }
        for (Integer currentId : current) {
            Integer idx = temp.indexOf(currentId);
            if(idx==-1){
                insert.add(currentId);
            }
        }
        List<DataFrameTreeBean> insertList = this.insertTreeBeanList(insert,frameId);
        List<DataFrameTreeBean> deleteList = this.deleteTreeBeanList(delete,treed);
        this.baseService.getRepo().deleteAll(deleteList);
        this.baseService.getRepo().saveAll(insertList);
    }

    @Transactional
    public void updateCore(DataFrameBean ext){
        Integer frameId = ext.id;
        DataFrameCategoryEntity exp = new DataFrameCategoryEntity();
        exp.frameId = frameId;
        List<DataFrameCategoryEntity> remove = this.baseService.find(exp);
        this.baseService.getRepo().deleteAll(remove);

        Integer coreId = ext.getCore().id;
        DataBean dataBean = baseService.get(DataBean.class, coreId);
        DataItemBean itemExp = new DataItemBean();
        itemExp.setData(dataBean);
        List<DataItemBean> dataItemBeans = baseService.find(itemExp);
        dataItemBeans.sort(Comparator.comparing(DataItemBean::getTimestamp).reversed());
        List<DataFrameCategoryEntity> list = new ArrayList<>();

        for (DataItemBean dataItemBean : dataItemBeans) {
            String category = dataItemBean.getCategory();
            DataFrameCategoryEntity cat = new DataFrameCategoryEntity();
            cat.setCategory(category);
            cat.setChecked(true);
            cat.setFrameId(frameId);

            list.add(cat);
        }

        this.baseService.getRepo().saveAll(list);
        long timestamp = new Date().getTime();
        for (DataFrameCategoryEntity dataFrameCategoryEntity : list) {
            dataFrameCategoryEntity.setTimestamp(new Date(timestamp));
            timestamp=timestamp-1000;
        }

    }

    @Override
    public void beforeUpdate(DataFrameBean ext, DataFrameBean bean) {
        if(ext.getData()!=null) this.updateDataTree(ext);
        if(ext.getCore()!=null) this.updateCore(ext);
    }

    private List<DataFrameTreeBean> insertTreeBeanList(List<Integer> insert, Integer frameId){
        List<DataFrameTreeBean> list = new ArrayList<>();
        for (Integer dataId : insert) {
            DataFrameTreeBean bean = new DataFrameTreeBean();
            bean.nodeType = 2;
            bean.pid = 0;
            bean.frameId = frameId;
            DataBean dataBean = this.baseService.get(DataBean.class, dataId);
            bean.setData(dataBean);
            list.add(bean);
        }
        return list;
    }

    private List<DataFrameTreeBean> deleteTreeBeanList(List<Integer> delete, Map<Integer,Integer> map){
        List<DataFrameTreeBean> list = new ArrayList<>();
        for (Integer dataId : delete) {
            Integer frameId = map.get(dataId);
            DataFrameTreeBean bean = new DataFrameTreeBean();
            bean.setId(frameId) ;
            list.add(bean);
        }
        return list;
    }

    private Map<Integer,Integer> getTreed(Integer frameId){
        Map<Integer,Integer> map = new HashMap<>();
        DataFrameTreeBean treeBean = new DataFrameTreeBean();
        treeBean.frameId = frameId;
        treeBean.nodeType = 2;
        List<DataFrameTreeBean> list = this.baseService.find(treeBean);
        for (DataFrameTreeBean tree : list) {
            Integer dataId = tree.getData().getId();
            Integer treeId = tree.getId();
            map.put(dataId,treeId);
        }
        return map;
    }

    private List<Integer> getCurrent (DataFrameBean ext){
        List<Integer> list = new ArrayList<>();
        List data = ext.getData();
        for (Object datum : data) {
            BaseEntity bean = (BaseEntity) datum;
            list.add(bean.getId());
        }
        return list;
    }

}
