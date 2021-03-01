package com.vala.framework.data.bean;

import com.vala.base.entity.BaseEntity;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
public class DataFrameCategoryEntity extends BaseEntity {
    public String category;
    public Boolean checked;
    public Integer frameId;
}
