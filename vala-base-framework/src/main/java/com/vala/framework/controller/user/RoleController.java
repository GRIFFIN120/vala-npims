package com.vala.framework.controller.user;

import com.vala.base.controller.BaseController;
import com.vala.framework.entity.user.RoleBasic;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/role")
public class RoleController extends BaseController<RoleBasic> {


}
