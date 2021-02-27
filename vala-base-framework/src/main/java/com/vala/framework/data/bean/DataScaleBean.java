package com.vala.framework.data.bean;

import com.vala.base.entity.TreeEntity;
import lombok.Data;
import lombok.ToString;

import javax.persistence.Entity;

@Entity
@Data
@ToString(callSuper = true)
public class DataScaleBean extends TreeEntity {

}
