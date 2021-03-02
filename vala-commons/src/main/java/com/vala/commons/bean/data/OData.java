package com.vala.commons.bean.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Map;

public class OData {

    public String name;
    public List<String> titles;
    public List<Map<String,Object>> data;

    public OData() {
    }

    public OData(String name, List<String> titles, List<Map<String, Object>> data) {
        this.name = name;
        this.titles = titles;
        this.data = data;
    }
}
