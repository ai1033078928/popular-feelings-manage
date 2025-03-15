package com.hrbu.service.impl;

import com.hrbu.dao.CommentMapper;
import com.hrbu.model.Comment;
import com.hrbu.service.ICommentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CommentServiceImpl implements ICommentService {
    @Resource
    CommentMapper commentMapper;

    @Override
    public List<Comment> selectByMessageId(String messageId) {
        return commentMapper.selectByMessageId(messageId);
    }
}
