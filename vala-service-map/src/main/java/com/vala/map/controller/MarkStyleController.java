package com.vala.map.controller;

import com.vala.base.controller.BaseController;
import com.vala.map.entity.MarkStyle;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mark")
public class MarkStyleController extends BaseController<MarkStyle> {
}
