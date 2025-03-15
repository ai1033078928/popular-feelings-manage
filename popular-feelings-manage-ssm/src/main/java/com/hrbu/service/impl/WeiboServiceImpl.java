package com.hrbu.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hrbu.dao.JoinTopNMapper;
import com.hrbu.dao.WeiboMapper;
import com.hrbu.model.TopicNum;
import com.hrbu.model.Topn;
import com.hrbu.model.Weibo;
import com.hrbu.service.IWeiboService;
import com.hrbu.component.ComparatorTopN;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
public class WeiboServiceImpl implements IWeiboService {
    @Resource
    WeiboMapper weiboMapper;
    @Resource
    JoinTopNMapper joinTopNMapper;

    /**
     * 查找所有微博
     * @return
     */
    @Override
    public PageInfo<Weibo> selectLikeData(int pageNum, String title) {
        int pageSize = 15;
        // 分页插件进行分页 PageHelper插件一定要在调用Mapper类方法上面用，并且是只会生效一次，如果要调用多个Mapper类方法那么你就在那个Mapper方法上再写一次PageHelper.startpage(x,x);
        // Page{count=true, pageNum=1, pageSize=10, startRow=0, endRow=10, total=0, pages=0, reasonable=null, pageSizeZero=null}[]
        PageHelper.startPage(pageNum, pageSize);
        List<Weibo> weibos = weiboMapper.selectLikeTitle(title); // selectAllData();
        PageInfo pageInfo = new PageInfo(weibos);
        //System.out.println(pageInfo.toString());
        return pageInfo;
    }

    /**
     * 查找热度前n的微博
     * @return
     */
    @Override
    public List<Topn> selectTopN() {
        List<Topn> topns = joinTopNMapper.selectTopN();
        // 创建比较器对象并排序
        Collections.sort(topns, new ComparatorTopN());
        return topns;
    }

    /**
     * 话题数目
     */
    @Override
    public List<TopicNum> getTopicNum(String startTime, String stopTime) {
        return weiboMapper.getTopicNum(startTime, stopTime);
    }

    @Override
    public List<Weibo> getTopicInfo(String title) {
        return weiboMapper.selectLikeTitle(title);
    }

    @Override
    public List<Weibo> getOneTopicNumInfo(String title, String startTime, String stopTime) {
        System.out.println(title + "---" + startTime + "----" + stopTime);
        return weiboMapper.getOneTopicNumInfo(title, startTime, stopTime);
    }
}
