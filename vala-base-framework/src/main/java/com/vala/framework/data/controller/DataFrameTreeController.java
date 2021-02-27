package com.vala.framework.data.controller;

import com.vala.base.controller.BaseController;
import com.vala.framework.data.bean.DataBean;
import com.vala.framework.data.bean.DataFrameTreeBean;
import com.vala.framework.user.entity.UserBasic;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data-frame-tree")
public class DataFrameTreeController extends BaseController<DataFrameTreeBean> {
}
