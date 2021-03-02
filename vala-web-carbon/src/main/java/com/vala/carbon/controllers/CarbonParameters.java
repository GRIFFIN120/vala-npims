package com.vala.carbon.controllers;

import com.vala.base.entity.BaseEntity;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
public class CarbonParameters extends BaseEntity {
    public Integer dataId;
    public String category;
    public Integer parameter;

    public Integer filterId;



}
