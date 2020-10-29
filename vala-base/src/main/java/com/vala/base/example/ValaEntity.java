package com.vala.base.example;

import com.vala.base.entity.BaseEntity;
import lombok.Data;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Data
@ToString(callSuper = true)
public class ValaEntity extends BaseEntity {
    @Column
    public String vala;

}
