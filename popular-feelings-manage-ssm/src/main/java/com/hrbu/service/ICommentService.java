package com.hrbu.service;

import com.hrbu.model.Comment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ICommentService {
    List<Comment> selectByMessageId(String messageId);
}
