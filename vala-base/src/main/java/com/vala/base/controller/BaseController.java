package com.vala.base.controller;

import com.vala.base.bean.SearchBean;
import com.vala.base.bean.SearchResult;
import com.vala.base.entity.BaseEntity;
import com.vala.base.entity.TreeEntity;
import com.vala.base.service.BaseService;
import com.vala.base.service.FastDfsService;
import com.vala.commons.bean.KV;
import com.vala.commons.bean.ResponseResult;
import com.vala.commons.util.BeanUtils;
import com.vala.commons.util.Constants;
import com.vala.base.utils.TreeUtils;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.Query;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class BaseController<T extends BaseEntity> extends BaseControllerWraper<T> implements ApplicationListener<ContextRefreshedEvent> {
    public Class<T> domain;
    @Autowired
    public BaseService<T> baseService;
    @Autowired
    FastDfsService fastDfsService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        domain = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        baseService.setDomain(domain);
    }

    @Transactional
    @RequestMapping("/update/many/{field}/{id}")
    public ResponseResult distinct(@PathVariable Integer id, @PathVariable String field , @RequestBody List<BaseEntity> list) throws Exception {

        Field f = domain.getDeclaredField(field);
        if(f.getType().equals(List.class)){
            T bean = this.baseService.get(id);
            if(bean!=null){
                f.set(bean,list);
                this.baseService.saveOrUpdate(bean);
            }else{
                return new ResponseResult(500,"数据未找到!");
            }
        }else{
            return new ResponseResult(500,"字段类型错误!");
        }
        return new ResponseResult(200);
    }


    @RequestMapping("/distinct/{field}")
    public ResponseResult distinct(@PathVariable String field){

        String sql = "SELECT DISTINCT("+field+") FROM " + this.domain.getSimpleName();
        Query nativeQuery = this.baseService.getEntityManager().createQuery(sql);
        List<String> resultList = nativeQuery.getResultList();
        List<KV> list = new ArrayStack();
        for (String s : resultList) {
            KV k = new KV();
            k.setName(s);
            list.add(k);
        }
        return new ResponseResult(list);
    }

    @RequestMapping("/simple")
    public ResponseResult distinct(){
        String sql = "SELECT id,name FROM " + this.domain.getSimpleName();
        Query nativeQuery = this.baseService.getEntityManager().createQuery(sql);
        List<Object[]> resultList = nativeQuery.getResultList();
        List<KV> list = new ArrayStack();
        for (Object[] s : resultList) {
            KV k = new KV();
            k.setId(s[0]);
            k.setName(s[1]);
            list.add(k);
        }
        return new ResponseResult(list);
    }

    @ResponseBody
    @RequestMapping("/fields")
    public ResponseResult fields() throws Exception {
        // 搜索前校验
        List<String> list = new ArrayList<>();
        List<Field> completeFields = BeanUtils.getCompleteFields(domain);
        for (Field completeField : completeFields) {
            list.add(completeField.getName());
        }
        return new ResponseResult(list);
    }


    @Transactional
    @ResponseBody
    @RequestMapping("/dfs/text/set/{id}/{field}")
    public ResponseResult dfsSet(@PathVariable Integer id, @PathVariable String field, @RequestBody String text) throws Exception {
        // 搜索前校验

        T bean = this.baseService.get(id);
        String path = (String) BeanUtils.get(bean, field);
        if(StringUtils.isNotEmpty(path)){
            fastDfsService.delete(path);
        }
        path = fastDfsService.upload(text.getBytes(), "txt");
        BeanUtils.set(bean,field,path);
        this.baseService.saveOrUpdate(bean);
        return new ResponseResult(200);
    }

    @Transactional
    @ResponseBody
    @RequestMapping("/dfs/text/get/{id}/{field}")
    public ResponseResult dfsGet(@PathVariable Integer id, @PathVariable String field) throws Exception {
        // 搜索前校验
        T bean = this.baseService.get(id);
        String path = (String) BeanUtils.get(bean, field);
        byte[] read = this.fastDfsService.read(path);
        String txt = new String(read);
        return new ResponseResult(200,null,txt);
    }



    @ResponseBody
    @RequestMapping("/search")
    public ResponseResult search(@RequestBody SearchBean<T> params) throws Exception {
        // 搜索前校验
        String message = this.beforeSearch(params);
        if(message!=null){ // 校验不通过
            return new ResponseResult(400, message);
        }else { // 校验通过
            SearchResult<T> result = baseService.search(params);
            // 更新后校验
            message = this.afterSearch(result);
            return message==null? new ResponseResult(result): new ResponseResult(400, message);
        }
    }

    @ResponseBody
    @RequestMapping("/search/tree")
    public ResponseResult tree(@RequestBody SearchBean<T> params) throws Exception {
        if(TreeEntity.class.isAssignableFrom(domain)){
            SearchBean searchBean = params;
            SearchResult<TreeEntity> result = baseService.search(searchBean);
            List<TreeEntity> results = result.getList();
            List<TreeEntity> treeEntities = TreeUtils.treeBean(results);
            return new ResponseResult(treeEntities);

        }else {
            return new ResponseResult(400, "数据结构不是树形");
        }



    }



    @Transactional
    @ResponseBody
    @RequestMapping("/insert")
    public ResponseResult<T> insert(@RequestBody T ext) throws Exception {
        // 更新前校验
        String message = this.beforeInsert(ext);
        if(message!=null){ // 校验不通过
            return new ResponseResult(400, message);
        }else{ // 校验通过
            T bean = baseService.saveOrUpdate(ext);
            // 更新后校验
            message = this.afterInsert(bean);
            return message==null? new ResponseResult("添加成功", bean): new ResponseResult(400, message);
        }
    }

    @ResponseBody
    @RequestMapping("/get/{id}")
    public ResponseResult<T> get(@PathVariable Integer id) throws Exception {
        T bean  = baseService.get(id);
        bean = afterGet(bean);
        return new ResponseResult<>(bean);
    }


    @ResponseBody
    @Transactional
    @RequestMapping("/update")
    public ResponseResult<T> update(@RequestBody T ext)  {

        Integer id = ext.getId();
        if(id==null){
            return new ResponseResult<>(400,"主键不能为空");
        }else{
            T bean = baseService.get(id);
            if(bean==null)   return new ResponseResult<>(400, "数据不存在");
            // 更新前校验
            String message = beforeUpdate(ext, bean);
            if(message!=null){
                return new ResponseResult<>(400,message);
            }else {
                BeanUtils.extend(domain, bean, ext);
                bean = this.baseService.saveOrUpdate(bean);
                // 更新后校验
                message =  this.afterUpdate(bean);
                return message==null? new ResponseResult("更新成功", bean): new ResponseResult(400, message);
            }
        }
    }

    @Transactional
    @ResponseBody
    @RequestMapping("/delete/{id}")
    public ResponseResult<T> delete(@PathVariable Integer id) throws Exception {
        T bean  = baseService.get(id);

        this.fastDfsService.deleteBeanFile(bean);

        if(bean==null)   return new ResponseResult<>(400, "数据不存在");
        // 删除前校验
        String message = this.beforeDelete(bean);
        if(message!=null){
            return new ResponseResult<>(400,message);
        }else {
            baseService.delete(id);
            message = this.afterDelete(bean);
            return new ResponseResult<T>(message);
        }
    }


    @Transactional
    @ResponseBody
    @RequestMapping("/deleteAll/{ids}")
    public ResponseResult<T> delete(@PathVariable List<Integer> ids) throws Exception {
        int count = this.baseService.delete(ids);
        return new ResponseResult<T>(String.format("成功删除 %s 条数据",count));
    }


    @Transactional
    @ResponseBody
    @RequestMapping("/order/{id}/{direction}")
    public ResponseResult<T> order(@PathVariable Integer id, @PathVariable String direction, @RequestBody T ext) throws Exception {

        String conditions = BeanUtils.bean2conditon(ext);
        Integer sid = this.baseService.getSibling(id,direction,conditions);
        if(sid==null){
            String message = null;
            int code = 400;
            if(Constants.SORT_UP.equalsIgnoreCase(direction)){
                message = "已经到达顶端";
            }else if(Constants.SORT_DOWN.equalsIgnoreCase(direction)){
                message = "已经到达底端";
            }else{
                message = "未知的排序参数:" + direction;
            }
            return new ResponseResult<T>(code,message);
        }else{
            this.baseService.order(id,sid);
            return new ResponseResult<T>(String.format("移动成功"));
        }


    }

    /* direction = before / after */
    @Transactional
    @ResponseBody
    @RequestMapping("/place/{draggingId}/{dropId}/{direction}")
    public ResponseResult<T> place(@PathVariable Integer draggingId, @PathVariable Integer dropId, @PathVariable String direction) throws Exception {
        if(TreeEntity.class.isAssignableFrom(domain)){
            T dp = this.baseService.get(dropId);     // 静
            T dg = this.baseService.get(draggingId); // 动

            TreeEntity drop = (TreeEntity) dp;  // 静
            TreeEntity dragging = (TreeEntity) dg;  // 动


            SearchBean<T> search = new SearchBean<>();
            TreeEntity exact = (TreeEntity) this.baseService.newInstance();
            exact.setPid(drop.getPid());    // 查找  drop（静） 的父节点下的所有节点
            search.setExact((T) exact);
            SearchResult<T> res = this.baseService.search(search);
            List<T> list = res.getList();

            List<Date> date = new ArrayList<>();

            int dpIndex = -1 ;
            int dgIndex = -1 ;


            for (int i = 0; i < list.size(); i++) {
                T t =  list.get(i);
                date.add(t.getTimestamp());
                dpIndex = (t.getId().equals(drop.id) && dpIndex==-1) ? i : dpIndex;
                dgIndex = (t.getId().equals(dragging.id) && dgIndex==-1) ? i : dgIndex;
            }






            if(dgIndex!=-1){
                list.remove(dgIndex);
                for (int i = 0; i < list.size(); i++) {
                    T t =  list.get(i);
                    if(t.id.equals(dp.getId())){
                        dpIndex = i;
                        break;
                    }
                }
            }else {
                date.add(dg.getTimestamp());
            }





            if("before".equals(direction)){
                list.add(dpIndex,dg);
            }else {
                list.add(dpIndex+1,dg);
            }




            date.sort(Comparator.comparingLong(Date::getTime).reversed());
            dragging.setPid(drop.getPid());
            for (int i = 0; i < date.size(); i++) {
                Date date1 =  date.get(i);
                T t = list.get(i);
                t.setTimestamp(date1);
                this.baseService.saveOrUpdate(t);
            }




            return new ResponseResult<T>("移动成功",dp);
        }else {
            return new ResponseResult(400, "数据结构不是树形");
        }






    }



}
