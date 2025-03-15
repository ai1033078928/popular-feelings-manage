package com.hrbu.controller;

import com.hrbu.annotation.Operation;
import com.hrbu.model.MyCode;
import com.hrbu.model.User;
import com.hrbu.service.IUserService;
import com.hrbu.util.MD5Util;
import com.hrbu.util.UniqueOrderGenerate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    @Resource
    IUserService userService;

    @Operation("跳转到index页面")
    @RequestMapping("/index")
    public String gotoIndex(){
        return "index";
    }

    @Operation("跳转到用户列表页")
    @RequestMapping("/userlist")
    public String gotoUserList(){
        return "userlist";
    }

    @Operation("跳转到登录页面")
    @RequestMapping("/login")
    public String gotoLogin(){
        return "login";
    }

    @Operation("跳转到注册页面")
    @RequestMapping("/register")
    public String gotoRegister(){ return "register"; }

    /**
     * 用户登录
     * @param phone
     * @param password
     * @return
     */
    @Operation("用户登录")
    @ResponseBody
    @RequestMapping("/user")
    public Map<String, Object> userLogin(@RequestParam("username")String phone,
                                         @RequestParam("password")String password,
                                         HttpSession session){
        Map<String, Object> map = new HashMap<>();
        // 此处username为手机号
        User selectUser = userService.selectByPhone(phone);
        if (selectUser == null) {
            map.put("code", new MyCode(100)); // 没有该用户
        } else if (selectUser.getPassword().equals(MD5Util.getMD5(password))) {
            map.put("code", new MyCode(97));
            session.setAttribute("loginUser", selectUser);
        } else {
            map.put("code", new MyCode(101)); // 密码错误
        }
        return map;
    }

    /**
     * 遍历用户
     * @return
     */
    @ResponseBody
    @RequestMapping("/list")
    public Map<String, Object> getAllUser(){
        Map<String, Object> map = new HashMap<>();
        List<User> userList = userService.getAllUser();
        map.put("code", new MyCode(99));
        map.put("userList", userList);
        return map;
    }

    /**
     * 用户注册
     * @param user
     * @return
     */
    @Operation("用户注册")
    @ResponseBody
    @RequestMapping("/adduser")
    public Map<String, Object> addUser(User user){
        Map<String, Object> map = new HashMap<>();
        if(userService.selectByPhone(user.getPhone()) != null){
            // 用户存在
            map.put("code", new MyCode(95));
        }else{
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            UniqueOrderGenerate id = new UniqueOrderGenerate(3, 5);
            String userId = String.valueOf(id.nextId());

            user.setPassword(MD5Util.getMD5(user.getPassword()));
            user.setRegistrationTime(df.format(new Date()));
            user.setUserId(userId);
            user.setUserName(userId);

            int insert = userService.addUser(user);  // 返回影响条数
            if(insert == 1){
                map.put("code", new MyCode(99));
            }else{
                map.put("code", new MyCode(96));
            }
        }
        return map;
    }

    /**
     * 删除用户
     * @param userId
     * @param phone
     * @return
     */
    @Operation("删除用户")
    @ResponseBody
    @RequestMapping("/deleteuser")
    public Map<String, Object> deleteUser(String userId, String phone){
        Map<String, Object> map = new HashMap<>();
        if(userService.selectByPhone(phone) != null){
            int insert = userService.deleteUser(userId);  // 返回影响条数
            if(insert == 1){
                map.put("code", new MyCode(99));
            }else{
                map.put("code", new MyCode(96));
            }
        }else{
            map.put("code", new MyCode(100));
        }
        return map;
    }

    /**
     * 按id查找用户
     * @param userId
     * @return
     */
    @ResponseBody
    @RequestMapping("/selectuser")
    public Map<String, Object> selectUser(String userId){
        Map<String, Object> map = new HashMap<>();
        // 查找用户信息 返回前台
        User user = userService.selectById(userId);
        map.put("user", user);
        return map;
    }

    /**
     * 修改用户
     * @param user
     * @return
     */
    @Operation("修改用户信息")
    @ResponseBody
    @RequestMapping("/updateuser")
    public Map<String, Object> updateUser(User user){
        Map<String, Object> map = new HashMap<>();
        // 先查找
        User userTemp = userService.selectById(user.getUserId());
        // 如果能找到
        if (userTemp != null){
            // 把注册时间赋值给新的user
            user.setRegistrationTime(userTemp.getRegistrationTime());
            int update = userService.updateById(user);
            if(update == 1){
                // 修改成功
                map.put("code", new MyCode(99));
            }else{
                // 修改失败
                map.put("code", new MyCode(96));
            }
        }else{
            // 未找到用户
            map.put("code", new MyCode(100));
        }
        return map;
    }

    /**
     * 退出
     * @return
     */
    @Operation("用户退出")
    @ResponseBody
    @RequestMapping("/logout")
    public String userExit(HttpSession session, SessionStatus sessionStatus){
        session.invalidate();
        sessionStatus.setComplete();
        return "index";
    }
}
