package com.vala.base.controller;


import com.vala.base.bean.SearchBean;
import com.vala.base.bean.SearchResult;

public class BaseControllerWraper<T> {



    public void beforeInsert(T ext){};

    public void beforeUpdate(T ext, T bean){}

    public void beforeDelete(T bean){}

    public T orderCondition(T bean){
        return bean;
    }

}
