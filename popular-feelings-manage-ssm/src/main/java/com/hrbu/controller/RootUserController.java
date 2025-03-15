package com.hrbu.controller;

import com.hrbu.annotation.Operation;
import com.hrbu.model.MyCode;
import com.hrbu.model.RootUser;
import com.hrbu.service.IRootUserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/root")
public class RootUserController {
    @Resource
    IRootUserService rootUserService;

    /**
     * 管理员用户登录
     * @param username
     * @param password
     * @return
     */
    @Operation("管理员登录")
    @ResponseBody
    @RequestMapping("/user")
    public Map<String, Object> manageUsers(String username, String password, HttpSession session){
        Map<String, Object> map = new HashMap<>();
        RootUser selectRootUser = rootUserService.selectByPrimaryKey(username);
        if (selectRootUser == null) {
            map.put("code", new MyCode(100)); // 没有该用户
        } else if (selectRootUser.getPassword().equals(password)) {
            map.put("code", new MyCode(98));
            session.setAttribute("loginRootUser", selectRootUser);
        } else {
            map.put("code", new MyCode(101)); // 密码错误
        }
        return map;
    }
}
