package com.vala.framework.meta.controller;

import com.vala.base.controller.BaseController;
import com.vala.framework.meta.entity.MetaOperEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meta-oper")
public class MetaOperController extends BaseController<MetaOperEntity> {



}
