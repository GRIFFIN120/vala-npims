package com.vala.framework.file.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Entity;

@Data
@Entity
@ToString(callSuper = true)
public class EditorImage extends ImageEntity {

    public Integer beanId;
    public String beanEntity;

}
