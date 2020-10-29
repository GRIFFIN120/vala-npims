package com.vala.base.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@MappedSuperclass//实体集成映射
public class TreeEntity<T extends TreeEntity> extends BaseEntity {

    @Column
    public Integer pid;

    @Column
    public Integer nodeType;

    @Transient
    public List<T> children = new ArrayList<>();

}
