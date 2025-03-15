package com.hrbu.dao;

import com.hrbu.model.RootUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RootUserMapper {
    int deleteByPrimaryKey(String id);

    int insert(RootUser record);

    int insertSelective(RootUser record);

    RootUser selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(RootUser record);

    int updateByPrimaryKey(RootUser record);
}