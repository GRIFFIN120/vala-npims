package com.vala.map.entity;

import com.vala.base.entity.BaseEntity;
import lombok.Data;
import lombok.ToString;

import javax.persistence.Entity;

@Entity
@Data
@ToString
public class PolygonStyle extends BaseEntity {

    public String strokeColor;
    public Double strokeOpacity;
    public Integer strokeWeight;

    public String fillColor;
    public Double fillOpacity;

    public String strokeStyle; //solid dashed
}
