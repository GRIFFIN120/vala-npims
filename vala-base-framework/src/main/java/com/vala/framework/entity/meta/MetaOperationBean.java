package com.vala.framework.entity.meta;

import com.vala.base.entity.BaseEntity;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
public class MetaOperationBean extends BaseEntity {

    public String entity;
    public String type;

    public Boolean enable;
    public Boolean sort;
    public Boolean insertion;
    public Boolean edit;
    public Boolean remove;
    public Boolean view;
    public Boolean pop;

}
