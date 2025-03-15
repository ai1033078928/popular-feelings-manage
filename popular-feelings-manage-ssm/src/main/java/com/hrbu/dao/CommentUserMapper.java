package com.hrbu.dao;

import com.hrbu.model.CommentUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentUserMapper {
    int deleteByPrimaryKey(String title);

    int insert(CommentUser record);

    int insertSelective(CommentUser record);

    CommentUser selectByPrimaryKey(String title);

    int updateByPrimaryKeySelective(CommentUser record);

    int updateByPrimaryKey(CommentUser record);
}