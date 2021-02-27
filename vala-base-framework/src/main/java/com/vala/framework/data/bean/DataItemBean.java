package com.vala.framework.data.bean;

import com.vala.base.entity.BaseEntity;
import com.vala.framework.user.entity.UserBasic;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Data
public class DataItemBean extends BaseEntity {

    private String category;
    private Double value;

//    private Integer dataId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="data_id")
    private DataBean data;


    @Override
    public String toString() {
        return category+"="+value;
    }
}
