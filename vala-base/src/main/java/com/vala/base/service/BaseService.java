package com.vala.base.service;


import com.vala.base.bean.SearchBean;
import com.vala.base.bean.SearchResult;
import com.vala.base.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.EntityManager;
import java.util.List;


public interface BaseService <T>{

    // 基础
    void setDomain(Class<T> clazz);
    T newInstance();
    T newInstance(Integer id);
    EntityManager getEntityManager();
    JpaRepository getRepo();






    // 增加/更改 INSERT
    T saveOrUpdate(T bean) throws Exception;



    // 自用 GET
    T get(Integer id) throws Exception;

    // 删除 DELETE
    boolean delete(Integer id) throws Exception;

    // 搜索
    SearchResult<T> search(SearchBean<T> search) throws Exception;


    // 扩展

    T load(T ext) throws Exception; // 将ext 附加在 bean 上
    T load(Integer id, T ext) throws Exception; // 将ext 附加在 bean 上
    void clear(T bean);   // 清空
    void order(Integer thisId, Integer thatId) throws Exception;
    <O extends BaseEntity> O get(Class<O> domain, Integer id);// 通用 GET
    <O extends BaseEntity> List<O> find(O bean);// 通用 GET


    Integer getSibling(Integer id, String direction, String condition);

}
