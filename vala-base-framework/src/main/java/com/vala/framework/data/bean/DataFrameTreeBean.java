package com.vala.framework.data.bean;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vala.base.entity.BaseEntity;
import com.vala.base.entity.TreeEntity;
import com.vala.framework.user.entity.UserBasic;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class DataFrameTreeBean extends TreeEntity {
    public Integer frameId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="data_id")
    private DataBean data;

    public Double parameter;
    public Double max;
    public Double predict;

    @Transient
    List<DataItemBean> dataItems;

//    @Transient
//    double[] tempData;


    @Transient
    public Double weight;

    @Transient
    public Double[] tempData;
}
