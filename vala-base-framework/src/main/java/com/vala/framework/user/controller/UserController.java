package com.vala.framework.user.controller;

import com.vala.base.controller.BaseController;
import com.vala.commons.bean.ResponseResult;
import com.vala.framework.user.entity.RoleBasic;
import com.vala.framework.user.entity.UserBasic;
import com.vala.framework.utils.JwtUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController<UserBasic> {

    @RequestMapping("/login")
    public ResponseResult<UserBasic> login(@RequestBody UserBasic user) throws Exception {
        List<UserBasic> list = this.baseService.find(user);
        if(list.size()!=0){
            UserBasic userBasic = list.get(0);
            String token = JwtUtils.sign(user.getUsername(),user.getId());
            userBasic.setToken(token);
            this.setSession("UID",userBasic.getId());
            String message = "登录成功！欢迎您，"+ userBasic.name;
            return new ResponseResult(message, userBasic);
        }else {
            return new ResponseResult(500,"用户不存在或密码不正确");
        }
    }
    @RequestMapping("/logout")
    public ResponseResult login() throws Exception {
        this.removeSession("UID");
        return new ResponseResult(200,"您已登出系统，欢迎再次使用。");
    }

    @RequestMapping("/changePassword")
    public ResponseResult changePassword(@RequestBody Map<String, Object> data) throws Exception {
        Integer id = (Integer) data.get("id");
        String former = (String) data.get("former");
        String current = (String) data.get("current");
        UserBasic user = new UserBasic();
        user.setId(id);
        user.setPassword(former);
        List<UserBasic> list = this.baseService.find(user);
        if(list.size()!=0){
            UserBasic userBasic = list.get(0);
            userBasic.setPassword(current);
            this.baseService.saveOrUpdate(userBasic);
            return new ResponseResult(200,"密码修改成功！",true);
        }else{
            return new ResponseResult(500,"密码错误！",false);
        }
    }



    @RequestMapping("/signUp")
    public ResponseResult signup(@RequestBody UserBasic user) throws Exception {
        UserBasic temp = new UserBasic();
        String username = user.getUsername();
        temp.setUsername(username);
        List<UserBasic> list = this.baseService.find(temp);
        if(list.size()!=0){
            return new ResponseResult(500,"邮箱"+username+"已经被注册，请使用其他邮箱注册。");
        }else{
            RoleBasic role = new RoleBasic();
            role.code = "user";
            List<RoleBasic> roleList = this.baseService.find(role);
            if(roleList.size()==0){
                this.baseService.getRepo().save(role);
                roleList.add(role);
            }
            user.setRoles(roleList);

            this.baseService.saveOrUpdate(user);
            return new ResponseResult(200,"注册成功!");
        }
    }

    @RequestMapping("/superLogin")
    public ResponseResult checkEmail(@RequestBody UserBasic user){
        String pwd = user.password;
        if(pwd.equals("19870120")){
            user.setId(0);
            String token = JwtUtils.sign(user.getUsername(),user.getId());
            user.setToken(token);
            return new ResponseResult(200,"注册成功!",user);
        }else{
            return new ResponseResult(499,"超管密码错误");
        }

    }



    @RequestMapping("/check")
    public Boolean checkEmail(@RequestParam String email){
        System.out.println(email);
        UserBasic userBasic = new UserBasic();
        userBasic.setUsername(email);
        List<UserBasic> list = this.baseService.find(userBasic);
        Boolean flag = list.size()==0;
        return flag;
//        return new ResponseResult(200,"注册成功!","abc");
    }

//    @Override
//    public UserBasic afterGet(UserBasic bean) {
//        bean.setPassword(null);
//        return bean;
//    }
}
