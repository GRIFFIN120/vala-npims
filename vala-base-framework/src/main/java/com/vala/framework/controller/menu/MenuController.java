package com.vala.framework.controller.menu;

import com.vala.base.controller.BaseController;
import com.vala.framework.entity.menu.MenuItem;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/menu")
public class MenuController extends BaseController<MenuItem> {



}
