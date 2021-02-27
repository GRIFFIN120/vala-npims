package com.vala.framework.data.controller;

import com.vala.base.controller.BaseController;
import com.vala.framework.data.bean.DataBean;
import com.vala.framework.menu.entity.MenuItem;
import com.vala.framework.user.entity.UserBasic;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data")
public class DataController extends BaseController<DataBean> {

    @Override
    public void beforeInsert(DataBean ext) {
        Integer uid = this.getSession("UID", Integer.class);
        UserBasic userBasic = this.baseService.get(UserBasic.class, uid);
        ext.setUser(userBasic);
    }
}
