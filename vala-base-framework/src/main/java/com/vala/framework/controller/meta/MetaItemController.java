package com.vala.framework.controller.meta;

import com.vala.base.bean.SearchBean;
import com.vala.base.bean.SearchResult;
import com.vala.base.controller.BaseController;
import com.vala.commons.bean.ResponseResult;
import com.vala.commons.util.BeanUtils;
import com.vala.framework.entity.meta.MetaBean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/meta")
public class MetaItemController extends BaseController<MetaBean> {

    @Transactional
    @RequestMapping("/add/{entity}/{type}/{list}")
    public ResponseResult loadALl(@PathVariable String entity, @PathVariable String type, @PathVariable List<String> list){


        System.out.println(list);

        Date timestamp = new Date();

        for (String name : list) {
            MetaBean m = new MetaBean();
            timestamp = new Date(timestamp.getTime()-1000);
            m.setType(type);
            m.setEntity(entity);
            m.setTool("input");
            m.setHideOnList(false);
            m.setReadOnly(false);
            m.setSortable(true);
            m.setProp(name);
            m.setLabel(name);
            m.setTimestamp(timestamp);
            this.baseService.saveOrUpdate(m);
        }



        List<Field> completeFields = BeanUtils.getCompleteFields(MetaBean.class);
        for (Field f : completeFields) {
            String name = f.getName();

        }

        return new ResponseResult(200);
    }


    @Transactional
    @RequestMapping("/copy")
    public ResponseResult copy(String fromEntity, String fromType, String toEntity, String toType) throws Exception {


        MetaBean meta = new MetaBean();
        meta.setEntity(fromEntity);
        meta.setType(fromType);
        SearchBean search = new SearchBean(meta);
        search.setDirection("asc");
        search.setSortColumn("timestamp");
        SearchResult<MetaBean> result = this.baseService.search(search);
        List<MetaBean> list = result.getList();





        for (MetaBean metaBean : list) {
            MetaBean clone = BeanUtils.clone(MetaBean.class, metaBean);
            clone.setId(null);
            clone.setEntity(toEntity);
            clone.setType(toType);
            this.baseService.saveOrUpdate(clone);
            clone.setTimestamp(metaBean.getTimestamp());
            this.baseService.saveOrUpdate(clone);
        }



        return new ResponseResult(200);
    }



}
