package com.vala.carbon.controllers.entity;

import com.vala.base.entity.FileColumn;
import com.vala.framework.data.bean.DataFrameBean;
import com.vala.base.entity.FileEntity;
import com.vala.framework.user.entity.UserBasic;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class CarbonFilterEntity extends FileEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="user_id")
    public UserBasic user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="frame_id")
    public DataFrameBean frame;


    public String state; // 计算中
    public Boolean expired;// 结果过期？
    // 因子筛选参数
    public Integer win;
    // 因子耦合预测参数
    public Integer ar;// arima-ar
    public Integer i;// arima-i
    public Integer ma; // arima-ma
    public Integer h; // 预测长度
    // 数据断点
    public String breakPoint;
    public Integer ncomp; // 主成分个数

    @FileColumn
    public String xiPath;
    @FileColumn
    public String xpPath;
    @FileColumn
    public String paramPath;


    @Transient
    public Boolean isPredictionAssigned;

    @Transient
    public Boolean isAdjustmentAssigned;

}
