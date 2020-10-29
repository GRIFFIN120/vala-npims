package com.vala.base.service;


import com.vala.base.bean.SearchBean;
import com.vala.base.bean.SearchResult;
import com.vala.base.entity.BaseEntity;

import javax.persistence.EntityManager;
import java.util.List;


public interface BaseService <T>{

    // 基础
    void setDomain(Class<T> clazz);
    T newInstance();
    T newInstance(Integer id);
    EntityManager getEntityManager();


    // 增加/更改 INSERT
    T saveOrUpdate(T bean);
    List<T> saveOrUpdate(List<T> bean);

    // 自用 GET
    T get(T bean);
    T get(Integer id);

    // 删除 DELETE
    boolean delete(Integer id) throws Exception;
    boolean delete(T bean) throws Exception;
    int delete(List<Integer> its) throws Exception;

    // 搜索
    SearchResult<T> search(SearchBean<T> search) ;
    SearchResult<T> search() ;

    // 扩展

    T load(T ext); // 将ext 附加在 bean 上
    T load(Integer id, T ext); // 将ext 附加在 bean 上
    void clear(T bean);   // 清空
    void order(Integer thisId, Integer thatId);
    <O extends BaseEntity> O get(Class<O> domain, Integer id);// 通用 GET
    Integer getSibling(Integer id, String direction, String condition);

}
