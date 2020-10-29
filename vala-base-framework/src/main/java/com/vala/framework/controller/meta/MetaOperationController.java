package com.vala.framework.controller.meta;

import com.vala.base.controller.BaseController;
import com.vala.framework.entity.meta.MetaOperationBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meta/operation")
public class MetaOperationController extends BaseController<MetaOperationBean> {



}
