package com.hrbu.dao;

import com.hrbu.model.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    int insert(Comment record);

    int insertSelective(Comment record);

    List<Comment> selectByMessageId(@Param("myMessageId") String messageId);

}