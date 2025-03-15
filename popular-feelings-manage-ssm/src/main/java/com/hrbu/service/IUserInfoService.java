package com.hrbu.service;

import com.hrbu.model.Userinfo;
import org.springframework.stereotype.Service;

@Service
public interface IUserInfoService {
    Userinfo selectByUId(String uid);
}
