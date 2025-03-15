package com.hrbu.service;

import com.hrbu.model.Location;
import org.springframework.stereotype.Service;

@Service
public interface ILocationCountService {
    Location selectByTitle(String title);
}
