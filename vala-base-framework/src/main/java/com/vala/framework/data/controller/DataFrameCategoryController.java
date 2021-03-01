package com.vala.framework.data.controller;

import com.vala.base.controller.BaseController;
import com.vala.framework.data.bean.DataFrameCategoryEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("data-frame-category")
public class DataFrameCategoryController extends BaseController<DataFrameCategoryEntity> {
}
