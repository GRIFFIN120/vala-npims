package com.vala.base.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vala.base.service.FastDfsService;
import com.vala.commons.bean.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/dfs")
public class DfsController {

    @Autowired
    FastDfsService fastDfsService;

    @RequestMapping("/load")
    public ResponseResult load(@RequestBody String json) throws Exception {

        ObjectMapper M = new ObjectMapper();
        Map<String, String> map = M.readValue(json, Map.class);
        String path = map.get("path");
        byte[] read = this.fastDfsService.read(path);
        String str =  new String(read);
        System.out.println(str);
        return new ResponseResult(200,null,str);
    }

}
