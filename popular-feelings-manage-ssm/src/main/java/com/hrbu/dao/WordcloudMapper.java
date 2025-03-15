package com.hrbu.dao;

import com.hrbu.model.Wordcloud;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WordcloudMapper {
    /**
     * 根据标题查找所有
     * @param title
     * @return
     */
    List<Wordcloud> selectByTitle(@Param("title1") String title);

    int insert(Wordcloud record);

    int insertSelective(Wordcloud record);
}