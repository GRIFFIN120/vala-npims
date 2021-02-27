package com.vala.framework.meta.controller;

import com.vala.base.bean.SearchBean;
import com.vala.base.bean.SearchResult;
import com.vala.base.controller.BaseController;
import com.vala.commons.bean.ResponseResult;
import com.vala.framework.meta.entity.Types;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/types")
public class TypeController extends BaseController<Types> {
    @RequestMapping("/domain/{domain}")
    public ResponseResult types(@PathVariable String domain) throws Exception {
        Types exact = this.baseService.newInstance();
        exact.domain = domain;
        SearchBean search = new SearchBean<Types>();
        search.setExact(exact);
        SearchResult<Types> result = this.baseService.search(search);
        return new ResponseResult(result.getList());
    }
}
