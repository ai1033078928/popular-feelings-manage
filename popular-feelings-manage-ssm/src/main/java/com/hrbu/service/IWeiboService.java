package com.hrbu.service;

import com.github.pagehelper.PageInfo;
import com.hrbu.model.TopicNum;
import com.hrbu.model.Topn;
import com.hrbu.model.Weibo;

import java.util.List;
import java.util.Map;

public interface IWeiboService {
    /**
     * 查找所有数据
     * @return
     */
    PageInfo<Weibo> selectLikeData(int pageNum, String title);

    /**
     * 查找热度前n的微博
     * @return
     */
    List<Topn> selectTopN();

    /**
     * 话题数目
     */
    List<TopicNum> getTopicNum(String startTime, String stopTime);

    List<Weibo> getTopicInfo(String title);

    List<Weibo> getOneTopicNumInfo(String title, String startTime, String stopTime);
}
