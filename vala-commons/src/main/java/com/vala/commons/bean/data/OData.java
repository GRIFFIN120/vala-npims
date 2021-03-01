package com.vala.commons.bean.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Map;

public class OData {

    public List<String> titles;
    // 输出Charts时使用
    public List<Map<String,Object>> list;

}
