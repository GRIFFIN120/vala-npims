package com.vala.framework.meta.entity;

import com.vala.base.entity.BaseEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Data
@Entity
public class MetaItemEntity extends BaseEntity {

    public String entity;
    public String type;


    public String prop;
    public String label;
    public Integer width;


    public Boolean sortable;
    public Boolean hideOnList;

    public Boolean readOnly;
    public Boolean hideOnForm;


    public String tool;

    public String toolParam;
    public String toolParamKey;

    public Boolean upload;
    public Boolean download;


    public Boolean editInTable;
    public Boolean hideOnInsert;
}
