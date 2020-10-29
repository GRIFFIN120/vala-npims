package com.vala.base.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JsonService<T> {

    private ObjectMapper mapper = null;
    {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<T> toList(String json, Class<T> clazz) throws Exception {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class, clazz);
        List<T> list = mapper.readValue(json, javaType);
        return list;
    }

    public T toBean(String json, Class<T> clazz) throws Exception {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class, clazz);
        return mapper.readValue(json,clazz);
    }

}
