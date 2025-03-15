package com.hrbu.dao;

import com.hrbu.model.Topn;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JoinTopNMapper {
    /**
     * 查找热度前n的微博
     * @return
     */
    List<Topn> selectTopN();
}
