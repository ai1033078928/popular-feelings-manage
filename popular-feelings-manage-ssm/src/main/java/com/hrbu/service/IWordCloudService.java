package com.hrbu.service;

import com.hrbu.model.Wordcloud;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IWordCloudService {
    List<Wordcloud> selectByTitle(String title);
}
