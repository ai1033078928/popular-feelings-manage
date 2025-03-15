package com.hrbu.dao;

import com.hrbu.model.Location;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LocationMapper {
    int insert(Location record);

    int insertSelective(Location record);

    List<Location> selectByTitle(String title);
}