package com.hrbu.service.impl;

import com.hrbu.dao.LocationMapper;
import com.hrbu.model.Location;
import com.hrbu.service.ILocationCountService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class LocationCountServiceImpl implements ILocationCountService {
    @Resource
    LocationMapper locationMapper;

    @Override
    public Location selectByTitle(String title) {
        return locationMapper.selectByTitle(title).get(0);
    }
}
