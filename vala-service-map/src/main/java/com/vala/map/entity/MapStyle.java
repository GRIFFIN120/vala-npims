package com.vala.map.entity;


import com.vala.base.entity.BaseEntity;
import lombok.Data;
import lombok.ToString;

import javax.persistence.Entity;

@Entity
@Data
@ToString
public class MapStyle extends BaseEntity {

    public Boolean bg;
    public Boolean road;
    public Boolean building;
    public Boolean point;

    public Double pitch;
    public Double rotation;
    public Double zoom;
    public String viewMode;
    public String lang;
    public Boolean showLabel;
    public String mapStyle;

    public Boolean scale;
    public Boolean toolBar;
    public Boolean overView;
    public Boolean controlBar;

    public String layer;
    public Double layerOpacity;

    public Boolean roadNet;
    public Boolean traffic;


}
