package com.vala.map.controller;

import com.vala.base.controller.BaseController;
import com.vala.map.entity.PolygonStyle;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/polygon")
public class PolygonStyleController extends BaseController<PolygonStyle> {
}
