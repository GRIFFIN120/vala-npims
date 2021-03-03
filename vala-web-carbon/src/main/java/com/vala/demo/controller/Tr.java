package com.vala.demo.controller;


import com.vala.base.entity.BaseEntity;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
@Data
@Entity
@ToString
public class Tr  extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    public Integer id;
    @Column
    public Integer phase;
    @Column
    public String hid;
    @Column
    public String tdate;
    @Column
    public Double size;

    @Column
    public Integer bef;
    @Column
    public Integer aft;
    @Column
    public Integer allo;
    @Column
    public Integer surr;
    @Column
    public Integer gap;
    @Column
    public Integer pos;
}
