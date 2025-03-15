package com.hrbu.service;

import com.hrbu.model.CommentUser;
import org.springframework.stereotype.Service;

@Service
public interface ICommentUser {
    CommentUser selectByTitle(String title);
}
