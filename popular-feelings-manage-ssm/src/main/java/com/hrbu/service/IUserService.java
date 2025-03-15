package com.hrbu.service;

import com.hrbu.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IUserService {
    List<User> getAllUser();

    User selectByPhone(String phone);

    int addUser(User user);

    int deleteUser(String userId);

    User selectById(String userId);

    int updateById(User user);

}
