package com.vala.base.controller;


import com.vala.base.bean.SearchBean;
import com.vala.base.bean.SearchResult;

public class BaseControllerWraper<T> {

    public String beforeSearch(SearchBean<T> params) {return null;}
    public String afterSearch(SearchResult<T> result) {return null;}

    public T afterGet(T bean){return  bean;}


    public String beforeInsert(T ext){return null;};
    public String afterInsert(T bean){return null;};

    public String beforeUpdate(T ext, T bean){return null;}
    public String afterUpdate(T bean)  {return null;}

    public String beforeDelete(T bean){return null;}
    public String afterDelete(T bean){return "删除成功";}

    public String beforeDeleteAll(T bean){return null;}
    public String afterDeleteAll(T bean){return null;}

    public String beforeLoad(T bean){
        return null;
    }

    public T orderCondition(T bean){
        return bean;
    }

}
