package com.hrbu.dao;

import com.hrbu.model.TopicNum;
import com.hrbu.model.Weibo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WeiboMapper {
    /**
     * 查询所有数据
     */
    @Select("select * from weibo")
    List<Weibo> selectAllData();

    /**
     * 数据模糊查询
     */
    List<Weibo> selectLikeTitle(@Param("myTitle") String title);

    /**
     * 话题数目
     */
    List<TopicNum> getTopicNum(@Param("startTime")String startTime, @Param("stopTime")String stopTime);

    int deleteByPrimaryKey(String messageid);

    int insert(Weibo record);

    int insertSelective(Weibo record);

    Weibo selectByPrimaryKey(String messageid);

    int updateByPrimaryKeySelective(Weibo record);

    int updateByPrimaryKey(Weibo record);

    List<Weibo> getOneTopicNumInfo(@Param("title")String title, @Param("startTime")String startTime, @Param("stopTime")String stopTime);
}