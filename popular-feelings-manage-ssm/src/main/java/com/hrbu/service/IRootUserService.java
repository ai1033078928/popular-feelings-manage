package com.hrbu.service;

import com.hrbu.model.RootUser;
import org.springframework.stereotype.Service;

@Service
public interface IRootUserService {

    RootUser selectByPrimaryKey(String id);

}
