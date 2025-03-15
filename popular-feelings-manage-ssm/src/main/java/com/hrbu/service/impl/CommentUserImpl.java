package com.hrbu.service.impl;

import com.hrbu.dao.CommentUserMapper;
import com.hrbu.model.CommentUser;
import com.hrbu.service.ICommentUser;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CommentUserImpl implements ICommentUser {
    @Resource
    CommentUserMapper commentUserMapper;

    @Override
    public CommentUser selectByTitle(String title) {
        return commentUserMapper.selectByPrimaryKey(title);
    }
}
