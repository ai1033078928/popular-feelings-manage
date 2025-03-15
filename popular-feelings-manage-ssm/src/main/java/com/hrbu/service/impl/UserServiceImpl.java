package com.hrbu.service.impl;

import com.hrbu.dao.UserMapper;
import com.hrbu.model.User;
import com.hrbu.service.IUserService;
import com.hrbu.util.UniqueId;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Service
public class UserServiceImpl implements IUserService {
    @Resource
    UserMapper userMapper;

    @Override
    public List<User> getAllUser() {
        List<User> users = userMapper.selectAllUser();
        return users;
    }

    @Override
    public User selectByPhone(String phone) {
        List<User> userList = userMapper.selectByPhone(phone);
        if(userList.size()>0){
            User user = userList.get(0);
            return user;
        }
        return null;
    }

    @Override
    public int addUser(User user) {
        user.setUserId(UniqueId.getUniqueId());
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        ft.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));  // 设置北京时区
        user.setRegistrationTime(ft.format(new Date()));
        return userMapper.insert(user);
    }

    @Override
    public int deleteUser(String userId) {
        return userMapper.deleteByPrimaryKey(userId);
    }

    @Override
    public User selectById(String userId) {
        return userMapper.selectByPrimaryKey(userId);
    }

    @Override
    public int updateById(User user) {
        return userMapper.updateByPrimaryKey(user);
    }
}
