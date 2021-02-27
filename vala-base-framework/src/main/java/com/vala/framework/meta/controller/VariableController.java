package com.vala.framework.meta.controller;

import com.vala.base.bean.SearchBean;
import com.vala.base.bean.SearchResult;
import com.vala.base.controller.BaseController;
import com.vala.commons.bean.ResponseResult;
import com.vala.framework.meta.entity.Types;
import com.vala.framework.meta.entity.Variables;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/variables")
public class VariableController extends BaseController<Variables> {

}
