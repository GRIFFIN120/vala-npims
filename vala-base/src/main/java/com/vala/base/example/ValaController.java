package com.vala.base.example;

import com.vala.base.controller.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vala")
class ValaController extends BaseController<ValaEntity> {


}
