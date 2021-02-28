package com.vala.framework.data.controller;

import com.vala.base.controller.BaseController;
import com.vala.commons.util.Constants;
import com.vala.framework.data.bean.DataGroupEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("data-group")
public class DataGroupController extends BaseController<DataGroupEntity> {

    @Override
    public void beforeInsert(DataGroupEntity ext) {
        String date = Constants.TIME_FORMAT.format(new Date());
        ext.date = date;
        super.beforeInsert(ext);
    }

    @Override
    public void beforeUpdate(DataGroupEntity ext, DataGroupEntity bean) {
        String date = Constants.TIME_FORMAT.format(new Date());
        ext.date = date;
        super.beforeUpdate(ext, bean);
    }
}
