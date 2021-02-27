package com.vala.commons.bean;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ChartBean {

    public List<String> titles;
    public Map<String, Double[]> data;

}
