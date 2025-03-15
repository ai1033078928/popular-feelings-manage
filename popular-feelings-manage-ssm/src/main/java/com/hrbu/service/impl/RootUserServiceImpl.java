package com.hrbu.service.impl;

import com.hrbu.dao.RootUserMapper;
import com.hrbu.model.RootUser;
import com.hrbu.service.IRootUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RootUserServiceImpl implements IRootUserService {
    @Resource
    RootUserMapper rootUserMapper;

    @Override
    public RootUser selectByPrimaryKey(String id) {
        RootUser rootUser = rootUserMapper.selectByPrimaryKey(id);
        return rootUser;
    }
}
