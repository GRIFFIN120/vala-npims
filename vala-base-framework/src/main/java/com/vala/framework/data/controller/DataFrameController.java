package com.vala.framework.data.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.vala.base.controller.BaseController;
import com.vala.base.entity.BaseEntity;
import com.vala.commons.bean.ResponseResult;
import com.vala.commons.util.Constants;
import com.vala.framework.data.bean.DataBean;
import com.vala.framework.data.bean.DataFrameBean;
import com.vala.framework.data.bean.DataFrameTreeBean;
import com.vala.framework.data.bean.DataItemBean;
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

    @Override
    public void beforeUpdate(DataFrameBean ext, DataFrameBean bean) {
        Integer frameId = ext.getId();
        if(ext.getData()==null) return;
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


        super.beforeUpdate(ext, bean);
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
