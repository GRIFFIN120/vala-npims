package com.vala.framework.controller.user;

import com.vala.base.controller.BaseController;
import com.vala.commons.bean.ResponseResult;
import com.vala.framework.entity.user.UserBasic;
import com.vala.framework.utils.JwtUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController<UserBasic> {

    @RequestMapping("/login")
    public ResponseResult login(@RequestBody UserBasic user){
        UserBasic userBasic = this.baseService.get(user);
        if(userBasic!=null){
            String token = JwtUtils.sign(user.getUsername(),user.getId());
            Map<String, Object> data = new HashMap<>();
            data.put("userInfo",userBasic);
            data.put("token",token);
            return new ResponseResult("登录成功", data);
        }else {
            return new ResponseResult(500,"用户不存在或密码不正确");
        }
    }

}
