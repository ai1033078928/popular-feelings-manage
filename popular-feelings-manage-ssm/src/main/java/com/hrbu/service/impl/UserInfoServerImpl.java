package com.hrbu.service.impl;

import com.hrbu.dao.UserinfoMapper;
import com.hrbu.model.Userinfo;
import com.hrbu.service.IUserInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserInfoServerImpl implements IUserInfoService {
    @Resource
    UserinfoMapper userinfoMapper;

    @Override
    public Userinfo selectByUId(String uid) {
        return userinfoMapper.selectByPrimaryKey(uid);
    }
}
