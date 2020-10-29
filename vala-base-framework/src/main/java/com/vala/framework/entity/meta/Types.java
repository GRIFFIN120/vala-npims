package com.vala.framework.entity.meta;


import com.vala.base.entity.BaseEntity;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
public class Types extends BaseEntity {
    public String code;
}
