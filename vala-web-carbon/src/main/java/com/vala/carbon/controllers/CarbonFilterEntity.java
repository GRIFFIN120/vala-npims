package com.vala.carbon.controllers;

import com.vala.framework.data.bean.DataFrameBean;
import com.vala.base.entity.FileEntity;
import com.vala.framework.user.entity.UserBasic;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Data
public class CarbonFilterEntity extends FileEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="user_id")
    public UserBasic user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="frame_id")
    public DataFrameBean frame;

    public Integer win;

    public String state; // 计算中

    public Boolean expired;

}
