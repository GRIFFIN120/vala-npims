package com.vala.framework.data.bean;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vala.base.convertor.EntityListSerializer;
import com.vala.base.entity.BaseEntity;
import com.vala.framework.user.entity.UserBasic;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@ToString(callSuper = true)
public class DataFrameBean extends BaseEntity {

    private Integer scaleId;
    private Integer unitId;

    // 核心字段（因变量）
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="core_id")
    private DataBean core;

    private Integer uid;

    // 所属用户
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="user_id")
    private UserBasic user;

    // 数据列表
    @Fetch(FetchMode.SUBSELECT)
    @JsonSerialize(using = EntityListSerializer.ListSerializer.class)
    @JsonDeserialize(using = EntityListSerializer.ListDeserializer.class)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="data_frame_data",
            joinColumns={@JoinColumn(name="data_frame_id")},
            inverseJoinColumns={@JoinColumn(name="data_id")}
    )
    public List<DataBean> data;

}
