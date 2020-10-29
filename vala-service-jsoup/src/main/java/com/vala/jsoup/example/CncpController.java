package com.vala.jsoup.example;

import com.vala.base.controller.BaseController;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.List;

@RestController
@RequestMapping("/cncp")
public class CncpController extends BaseController<Cncp> {

    @Autowired
    CncpService service;

    @Transactional
    @RequestMapping("/price")
    public String price() throws Exception {
        List<Object[]> price = service.getPrice();
        for (Object[] objects : price) {
            String line = StringUtils.join(objects,",");
            System.out.println(line);
        }
        return "price";
    }


    @RequestMapping("/stat")
    public String stat(){
        service.stat();
        return "price";
    }

}
