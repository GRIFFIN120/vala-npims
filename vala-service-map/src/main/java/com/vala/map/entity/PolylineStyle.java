package com.vala.map.entity;

import com.vala.base.entity.BaseEntity;
import lombok.Data;
import lombok.ToString;

import javax.persistence.Entity;

@Entity
@Data
@ToString
public class PolylineStyle extends BaseEntity {

    public Boolean isOutline;
    public Double borderWeight;
    public String outlineColor;

    public String strokeColor;
    public Double strokeOpacity;
    public Double strokeWeight;
    public String strokeStyle; //solid dashed

    public String lineJoin; //默认值为'miter'尖角，其他可选值：'round'圆角、'bevel'斜角
    public String lineCap; //默认值为'butt'无头，其他可选值：'round'圆头、'square'方头

}
