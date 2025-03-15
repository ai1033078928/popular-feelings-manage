package com.hrbu.service.impl;

import com.hrbu.dao.WordcloudMapper;
import com.hrbu.model.Wordcloud;
import com.hrbu.service.IWordCloudService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class WordCloudServiceImpl implements IWordCloudService {

    @Resource
    WordcloudMapper wordcloudMapper;

    @Override
    public List<Wordcloud> selectByTitle(String title) {
        List<Wordcloud> wordcloudList = wordcloudMapper.selectByTitle(title);
        return wordcloudList;
    }
}
