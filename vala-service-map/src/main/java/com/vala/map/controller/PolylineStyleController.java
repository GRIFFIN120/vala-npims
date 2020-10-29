package com.vala.map.controller;

import com.vala.base.controller.BaseController;
import com.vala.map.entity.PolylineStyle;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/polyline")
public class PolylineStyleController extends BaseController<PolylineStyle> {
}
