package com.vala.base.service;

import com.vala.base.bean.SearchBean;
import com.vala.base.bean.SearchResult;
import com.vala.base.entity.SimpleEntity;
import com.vala.commons.util.BeanUtils;
import com.vala.commons.util.Constants;
import com.vala.base.entity.BaseEntity;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.*;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.*;

@Service
@Scope("prototype")
public class BaseServiceImpl<T extends BaseEntity> implements BaseService<T> {


    // 资源加载
    @Resource
    private BaseRepo repo;
    @Override
    public JpaRepository getRepo() {
        return repo;
    }

    @PersistenceContext
    private EntityManager entityManager;
    @Override
    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    // 基础方法

    public Class<T> domain;
    @Override
    public void setDomain(Class<T> cls) {
        domain = cls;
    }
    @Override
    public T newInstance(){
        try {
            return domain.newInstance();
        } catch (Exception e) {
            return null;
        }
    }
    @Override
    public T newInstance(Integer id){
        T bean = this.newInstance();
        if(id != null && bean != null){
            bean.setId(id);
            return bean;
        }else {
            return null;
        }
    }

    // GET
    @Transactional
    @Override
    public T get(Integer id) throws Exception {
        T instance = this.newInstance(id);
        if(instance != null){
            Optional<T> op = this.getRepo().findOne(Example.of(instance));
            instance = op.isPresent() ? op.get() : null;
        }
        return instance;
    }

    // SaveOrUpdate
    @Transactional
    @Override
    public T saveOrUpdate(T bean) throws Exception {
        Class domain = this.domain;
        if(domain==null) domain = bean.getClass();

        // 对象字段更新（）
        Map<String,Object> map = new HashMap<>();
        List<Field> completeFields = BeanUtils.getCompleteFields(domain);
        for (Field field : completeFields) {
            if(BaseEntity.class.isAssignableFrom(field.getType())){
                field.setAccessible(true);
                String name = field.getName();
                Object value = field.get(bean);
                if(value!=null){
                    BaseEntity base = (BaseEntity) value;
                    Integer id = base.getId();
                    if(id!=null){
                        Optional one = this.getRepo().findOne(Example.of(base));
                        Object o = one.get();
                        map.put(name,o);
                    }
                }
            }
        }



        if (bean == null) return null;
        if(bean.getId()!=null){ // 更新
            T target = this.get(bean.id);
            BeanUtils.extend(domain, target, bean,ManyToOne.class);
            bean = target;
        }


        for (String name : map.keySet()) {
            Object value = map.get(name);
            Field field = domain.getDeclaredField(name);
            System.out.println(name+","+field);
            field.setAccessible(true);
            field.set(bean,value);
        }


        return (T) this.getRepo().save(bean);
    }


    // Delete
    @Override
    public boolean delete(Integer id) throws Exception{
        T bean = this.get(id);
        if(bean==null) return false;
        // 删除bean涉及的中间键（遵循规范）
        List<Field> m2m = BeanUtils.getFieldsByAnnotation(this.domain, ManyToMany.class);
        for (Field field : m2m) {
            ManyToMany annotation = field.getAnnotation(ManyToMany.class);
            String mappedBy = annotation.mappedBy();
            boolean isDominating = StringUtils.isEmpty(mappedBy);
            if(isDominating){
                if(field.getType().isAssignableFrom(List.class)){
                    field.set(bean,new ArrayList<>());
                }else if(field.getType().isAssignableFrom(Set.class)){
                    field.set(bean,new HashSet<>());
                }

            }else{
                Class mappedClass = BeanUtils.getFieldParameterizedType(domain,field.getName());
                Field mappedField = mappedClass.getField(mappedBy);
                Collection list = (Collection) field.get(bean);
                for (Object mappedBean : list) {
                    Collection mappedList = (Collection)  mappedField.get(mappedBean);
                    mappedList.remove(bean);
                }
            }
        }
        this.getRepo().save(bean);
        // 删除选中的bean
        this.getRepo().delete(bean);
        return true;
    }





    // Search
    @Transactional
    @Override
    public SearchResult<T> search(SearchBean<T> search) throws Exception {
        if(search==null){
            search = new SearchBean<>( this.newInstance());
        }

        Example<T> example = getExample(search.getExact(), search.getFuzzy());
        Sort.Order order = new Sort.Order(Sort.Direction.fromString(search.getDirection()), search.getSortColumn(), Sort.NullHandling.NULLS_LAST);
        Sort sort = Sort.by(order);

        Integer page = search.getPage();
        page = page<1? 1: page;
        Integer size = search.getSize();
        size = size<0? 0 :size;

        if(search.getSize()>0){
            Pageable pageable = PageRequest.of(page-1, size, sort);
            Page p = this.getRepo().findAll(example, pageable);
            List<T> list = p.getContent();
//            this.eraseLazy(list);
            return new SearchResult<T>(p.getTotalElements(), Long.valueOf(list.size()), page, size, p.getTotalPages(), search.getSortColumn(), search.getDirection(), list);
        }else {
            List<T> list = this.getRepo().findAll(example, sort);
            Long total = Long.valueOf(list.size());
            this.eraseLazy(list);
            return new SearchResult<T>(total, search.getSortColumn(), search.getDirection(), list);
        }
    }





    private void eraseLazy(List<T> list) throws Exception {
        List<Field> lzFs = BeanUtils.getLazyFieldInListForm(this.domain);
        for (T t : list) {
            for (Field lzF : lzFs) {
                lzF.set(t,null);
            }
        }
    }





    private Example<T> getExample(T exact, T fuzzy){
        // exact 不能为 null

        if(exact == null) exact = newInstance();
        if(fuzzy == null) fuzzy = newInstance();
        Example<T> example = Example.of(exact);
//        if(BeanUtils.isNotEmptyBean(fuzzy)){ // 最多结果原则： 如果同一字段在ext与fuzz都不为空，以fuzzy为准
            try {
                ExampleMatcher matcher = ExampleMatcher.matching();
                List<Field> fields = BeanUtils.getCompleteFields(domain);
                for (Field field : fields) {
                    field.setAccessible(true);
                    String column = field.getName();
                    Class<?> type = field.getType();
                    if(!type.isAssignableFrom(List.class)){// 无法处理 List 类型的字段
                        Object extVal = field.get(exact);
                        Object fuzVal = field.get(fuzzy);
                        if(fuzVal!=null && StringUtils.isNotBlank(fuzVal.toString())){
                            matcher = matcher.withMatcher(column, ExampleMatcher.GenericPropertyMatchers.contains());
                        }else if(extVal!=null && StringUtils.isNotBlank(extVal.toString())){
                            matcher = matcher.withMatcher(column, ExampleMatcher.GenericPropertyMatchers.exact());
                            field.set(fuzzy,extVal);
                        }
                    }
                }
                example = Example.of(fuzzy, matcher);
            } catch (Exception e) {
                e.printStackTrace();
                example = Example.of(exact);
            }
//        }
        return example;
    }


    // 扩展

    @Override
    public T load(T ext) throws Exception {
        T bean = null;
        if(ext.getId() != null){
            bean = this.load(ext.getId(),ext);
        }
        return bean;
    }

    @Override
    public T load(Integer id, T ext) throws Exception {
        T bean = this.get(ext.getId());
        if (bean != null) {
            BeanUtils.extend(domain, bean, ext, null);
        }
        return bean;
    }

    @Override
    public void clear(T bean) {
        List list = this.getRepo().findAll(Example.of(bean));
        this.getRepo().deleteAll(list);
    }

    @Transactional
    @Override
    public void order(Integer thisId, Integer thatId) throws Exception {
        T thisBean = this.get(thisId);
        T thatBean = this.get(thatId);
        Date temp = thisBean.getTimestamp();
        thisBean.setTimestamp(thatBean.getTimestamp());
        thatBean.setTimestamp(temp);
        this.getRepo().save(thisBean);
        this.getRepo().save(thatBean);
    }

    @Override
    public <O extends BaseEntity> O get(Class<O> domain, Integer id) {
        O bean = null;
        try {
            bean = domain.newInstance();
            bean.setId(id);
            Optional<O> op = this.getRepo().findOne(Example.of(bean));
            bean = op.isPresent() ? op.get() : null;
        } catch (Exception e) {
            bean = null;
        }
        return bean;
    }

    @Override
    public <O extends BaseEntity> List<O> find(O bean) {
        List all = this.getRepo().findAll(Example.of(bean));
        return all;
    }

    @Override
    public Integer getSibling(Integer id, String direction, String condition){
        String table = this.domain.getSimpleName();
        String sql = "select id from %s where timestamp = (select %s(timestamp) from %s where timestamp %s (select timestamp from %s where id=?1)  %s ) %s";
        if(Constants.SORT_UP.equalsIgnoreCase(direction)){
            sql = String.format(sql,table,"min",table,">",table,condition,condition);
        }else if(Constants.SORT_DOWN.equalsIgnoreCase(direction)){
            sql = String.format(sql,table,"max",table,"<",table,condition,condition);
        }else{
            return null;
        }
        Query query = entityManager.createQuery(sql);
        query.setParameter(1,id);

        try {
            return (Integer) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }

    }


}
