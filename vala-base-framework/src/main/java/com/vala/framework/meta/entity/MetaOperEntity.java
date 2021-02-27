package com.vala.framework.meta.entity;

import com.vala.base.entity.BaseEntity;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
public class MetaOperEntity extends BaseEntity {

    public String entity;
    public String type;

    public Boolean enable;
    public Boolean sort;
    public Boolean insertion;
    public Boolean edit;
    public Boolean remove;
    public Boolean view;
    public Boolean pop;
    public Boolean search;
    public Boolean buttons;
    public Boolean export;
    public Boolean upload;

}
