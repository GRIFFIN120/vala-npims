package com.vala.framework.meta.entity;


import com.vala.base.entity.TreeEntity;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
public class Variables extends TreeEntity {
    public String code;
}
