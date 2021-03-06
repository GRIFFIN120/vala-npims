package com.vala.framework.meta.controller;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.vala.base.bean.SearchBean;
import com.vala.base.bean.SearchResult;
import com.vala.base.controller.BaseController;
import com.vala.base.utils.XlsUtils;
import com.vala.commons.bean.KV;
import com.vala.commons.bean.ResponseResult;
import com.vala.commons.util.BeanUtils;
import com.vala.framework.meta.entity.MetaItemEntity;
import org.apache.commons.collections.ArrayStack;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.*;

@RestController
@RequestMapping("/meta-item")
public class MetaItemController extends BaseController<MetaItemEntity> {

    @Override
    public void beforeOutput(MetaItemEntity bean) {
        super.beforeOutput(bean);
    }

    @Override
    public void beforeUpdate(MetaItemEntity ext, MetaItemEntity bean) {
        super.beforeUpdate(ext, bean);
    }

    @RequestMapping("/counts/{entity}")
    public ResponseResult getCounts(@PathVariable String entity){
        String sql = "SELECT type,COUNT(type) FROM  MetaItemEntity WHERE entity = '"+entity+"' GROUP BY type";
        Query nativeQuery = this.baseService.getEntityManager().createQuery(sql);
        List<Object[]> resultList = nativeQuery.getResultList();
        Map map = new HashMap();

        for (Object[] os : resultList) {
            Object prop = os[0];
            Object count = os[1];
            map.put(prop,count);
        }
        return new ResponseResult(map);
    }




    @Transactional
    @RequestMapping("/add/{entity}/{type}/{list}")
    public ResponseResult loadALl(@PathVariable String entity, @PathVariable String type, @PathVariable List<String> list) throws Exception {

        Date timestamp = new Date();

        for (String name : list) {
            MetaItemEntity m = new MetaItemEntity();
            timestamp = new Date(timestamp.getTime()-1000);
            m.setType(type);
            m.setEntity(entity);
            m.setTool("input");
            m.setHideOnList(false);
            m.setHideOnForm(false);
            m.setReadOnly(false);
            m.setSortable(true);
            m.setDownload(true);
            m.setUpload(true);
            m.setWidth(0);
            m.setProp(name);
            m.setLabel(name);
            m.setTimestamp(timestamp);
            this.baseService.saveOrUpdate(m);
        }



        List<Field> completeFields = BeanUtils.getCompleteFields(MetaItemEntity.class);
        for (Field f : completeFields) {
            String name = f.getName();

        }

        return new ResponseResult(200);
    }


    @Transactional
    @RequestMapping("/copy/{entity}/{fromType}/{toType}")
    public ResponseResult copy(@PathVariable String entity,@PathVariable String fromType,@PathVariable String toType) throws Exception {


        MetaItemEntity meta = new MetaItemEntity();
        meta.setEntity(entity);
        meta.setType(fromType);
        SearchBean search = new SearchBean(meta);
        search.setDirection("asc");
        search.setSortColumn("timestamp");
        SearchResult<MetaItemEntity> result = this.baseService.search(search);
        List<MetaItemEntity> list = result.getList();


        for (MetaItemEntity metaBean : list) {
            MetaItemEntity clone = BeanUtils.clone(MetaItemEntity.class, metaBean);
            clone.setId(null);
            clone.setEntity(entity);
            clone.setType(toType);
            this.baseService.saveOrUpdate(clone);
            clone.setTimestamp(metaBean.getTimestamp());
            this.baseService.saveOrUpdate(clone);
        }


        return new ResponseResult(200,"复制成功");
    }



}
