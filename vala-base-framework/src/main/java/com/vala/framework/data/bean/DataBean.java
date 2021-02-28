package com.vala.framework.data.bean;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vala.base.entity.BaseEntity;
import com.vala.framework.user.entity.UserBasic;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class DataBean extends BaseEntity {

    private Integer scaleId;
    private Integer unitId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="group_id")
    private DataGroupEntity group;



    @JsonIgnore
    @OneToMany(mappedBy="data", cascade = {CascadeType.REMOVE},fetch = FetchType.LAZY)
    public List<DataItemBean> items = new ArrayList<>();


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="user_id")
    private UserBasic user;


    @JsonIgnore
    @ManyToMany(mappedBy = "data", fetch = FetchType.LAZY)
    public List<DataFrameBean> frames;
}
