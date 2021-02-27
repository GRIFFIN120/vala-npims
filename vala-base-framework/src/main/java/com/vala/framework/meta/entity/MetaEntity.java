package com.vala.framework.meta.entity;

import com.vala.base.entity.TreeEntity;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
public class MetaEntity extends TreeEntity {
    public String entity;
}
