package com.hrbu.dao;

import com.hrbu.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    int deleteByPrimaryKey(String userId);

    /**
     * 插入用户
     * @param record
     * @return
     */
    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(String userId);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    /**
     * 查找所有用户
     * @return
     */
    List<User> selectAllUser();

    /**
     * 通过手机号查找用户
     * @param phone
     * @return
     */
    List<User> selectByPhone(@Param("phone1") String phone);
}