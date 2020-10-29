package com.vala.map.controller;

import com.vala.base.controller.BaseController;
import com.vala.map.entity.MapStyle;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/map")
public class MapStyleController extends BaseController<MapStyle> {
}
