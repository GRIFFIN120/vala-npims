package com.vala.demo.controller;

import com.vala.base.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.transaction.Transactional;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class GuoEntity extends BaseEntity {
    public Integer due;
    public Integer valid;
    public Integer span;
    public Integer rate;
    public Double value;
    public Boolean immune;

    @Transient
    public Integer time;
    @Transient
    public List data;
    @Transient
    public String valueText;
}
