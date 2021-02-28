package com.vala.base.controller;


import com.vala.base.bean.SearchBean;
import com.vala.base.bean.SearchResult;

import java.util.List;

public class BaseControllerWraper<T> {


    public void beforeOutput(T bean){};
    public void beforeOutput(List<T> list){
        for (T t : list) {
            this.beforeOutput(t);
        }
    }


    public void beforeInsert(T ext){};

    public void beforeUpdate(T ext, T bean){}

    public void beforeDelete(T bean){}

    public T orderCondition(T bean){
        return bean;
    }

}
