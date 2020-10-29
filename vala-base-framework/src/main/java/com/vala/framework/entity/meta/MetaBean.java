package com.vala.framework.entity.meta;

import com.vala.base.entity.BaseEntity;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
public class MetaBean extends BaseEntity {
    public String prop;
    public String label;
    public String tool;
    public Integer width;
    public Boolean sortable;
    public Boolean readOnly;
    public Boolean hideOnList;
    public String entity;
    public String type;

    public String selectTypes;
    public String selectValueKey;

}
