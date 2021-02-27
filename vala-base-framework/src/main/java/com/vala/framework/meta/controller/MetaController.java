package com.vala.framework.meta.controller;

import com.vala.base.controller.BaseController;
import com.vala.framework.meta.entity.MetaEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meta")
public class MetaController extends BaseController<MetaEntity> {


}
