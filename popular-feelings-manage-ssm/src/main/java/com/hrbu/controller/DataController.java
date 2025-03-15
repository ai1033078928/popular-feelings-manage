package com.hrbu.controller;

import com.github.pagehelper.PageInfo;
import com.hrbu.annotation.Operation;
import com.hrbu.model.MyCode;
import com.hrbu.model.MyPage;
import com.hrbu.model.TopicNum;
import com.hrbu.model.Weibo;
import com.hrbu.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO 前端获取数据的控制器
@Controller
@RequestMapping("/data")
public class DataController {
    @Resource
    IWordCloudService iWordCloudService;
    @Resource
    ILocationCountService iLocationCountService;
    @Resource
    IWeiboService iWeiboService;
    @Resource
    IHBaseService ihBaseService;
    @Resource
    ICommentUser iCommentUser;
    @Resource
    ICommentService iCommentService;
    @Resource
    IUserInfoService iUserInfoService;

    /*--------------------------------分隔线(微博数据)----------------------------------------*/

    /**
     * 跳转到数据列表页
     */
    @Operation("跳转到数据列表页")
    @RequestMapping("/datalist")
    public String gotoDataList(){
        return "datalist";
    }

    /**
     * 获取数据列表信息
     */
    @ResponseBody
    @RequestMapping("/getdatalist")
    public Map<String, Object> getDataList(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "") String title){
        Map<String, Object> map = new HashMap<>();
        PageInfo<Weibo> weiboPageInfo = iWeiboService.selectLikeData(pageNum, title);
        if( weiboPageInfo != null ) {
            map.put("dataList", weiboPageInfo.getList());
            // 返回前端用于显示
            map.put("myPage", new MyPage(weiboPageInfo.getPageNum(), weiboPageInfo.getPageSize(), weiboPageInfo.getSize(),
                    weiboPageInfo.getStartRow(), weiboPageInfo.getEndRow(), weiboPageInfo.getPages()));
            map.put("title", title);
            //System.out.println("controller" + weiboPageInfo.toString());
            map.put("code", new MyCode(99));
        } else {
            map.put("code", new MyCode(96));
        }
        return map;
    }

    /**
     * 只返回一页数据
     */
    @ResponseBody
    @RequestMapping("/getPageData")
    public Map<String, Object> getPageData(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "") String title){
        Map<String, Object> map = new HashMap<>();
        PageInfo<Weibo> weiboPageInfo = iWeiboService.selectLikeData(pageNum, title);
        map.put("dataList", weiboPageInfo.getList());
        map.put("title", title);
        return map;
    }

    /*--------------------------------分隔线(热点话题)----------------------------------------*/

    /**
     * 跳转到热点话题页
     */
    @Operation("跳转到热点话题页")
    @RequestMapping("/topnlist")
    public String gotoTopnList(){
        return "topnlist";
    }

    /**
     * 获取热点话题
     */
    @ResponseBody
    @RequestMapping("/gettopnlist")
    public Map<String, Object> getTopnList(){
        Map<String, Object> map = new HashMap<>();
        map.put("topN", iWeiboService.selectTopN());
        return map;
    }

    /*--------------------------------分隔线(地区分布图)----------------------------------------*/

    /**
     * 跳转到地图页面(携带参数title)
     */
    @Operation("跳转到地图可视化页面")
    @RequestMapping("/gotolocation")
    public String gotoLocation(String title, Map<String, Object> map){
        //System.out.println(title);
        map.put("title", title);
        return "location_count";
    }

    /**
     * 获取地区的数据信息
     */
    @ResponseBody
    @RequestMapping("/getlocationcount")
    public Map<String, Object> getLocationCount(String title){
        //System.out.println(title);
        Map<String, Object> map = new HashMap<>();
        // 参数不为空
        if (!title.equals(null) && !"".equals(title)){
            map.put("code", new MyCode(99));
            map.put("locationCount", iLocationCountService.selectByTitle(title));
        } else{
            map.put("code", new MyCode(96)); // 未获取到参数
        }
        return map;
    }

    /*--------------------------------分隔线(词云图)----------------------------------------*/

    /**
     * 跳转到词云图页面(携带参数title)
     */
    @Operation("跳转到词云可视化页面")
    @RequestMapping("/gotowordcloud")
    public String gotoWordCloud(String title, Map<String, Object> map){
        map.put("title", title);
        return "word_cloud";
    }

    /**
     * 获取词语的数据
     */
    @ResponseBody
    @RequestMapping("/wordcloud")
    public Map<String, Object> getWordCloudData(String title){
        Map<String, Object> map = new HashMap<>();
        if (!title.equals(null) && !"".equals(title)){
            map.put("words", iWordCloudService.selectByTitle(title));
            map.put("code", new MyCode(99));
        }
        return map;
    }


    /**
     * 跳转到词云图页面2(携带参数title 从hbase获取的数据)
     */
    @Operation("跳转到hbase词云可视化页面")
    @RequestMapping("/gotohbasewordcloud")
    public String gotoHbaseWordCloud(String title, Map<String, Object> map){
        map.put("title", title);
        return "word_cloud_streaming";
    }

    /**
     * 获取词语的数据
     */
    @ResponseBody
    @RequestMapping("/hbasewordcloud")
    public Map<String, Object> getHbaseWordCloudData(@RequestParam(defaultValue = "") String title){
        Map<String, Object> map = new HashMap<>();
        if (!title.equals(null) && !"".equals(title)){
            map.put("words", ihBaseService.selectWordCloud(title));
            map.put("code", new MyCode(99));
        }
        return map;
    }


    /**
     * FIXME 图表: 热门话题 风险话题占比 负面评论占比 点赞排行
     * 话题数量 评论数量
     */

    /*--------------------------------分隔线(微博信息展示)----------------------------------------*/
    /**
     * 所有话题的话题数量
     */
    @RequestMapping("/gotoTopicMessage")
    public String topicNumber(Model model){
        // model.addAttribute("msg", "<span style='color:red'>警告</span>");  // 前端显示<p th:utext="'用户名称：' + ${name}"/>
        model.addAttribute("topics", iWeiboService.getTopicNum(null, null));
        return "topic_message";
    }
    @ResponseBody
    @RequestMapping("/getTopicMessage")
    public Map getTopicNumber(@RequestParam(value = "", required = false) String startTime,
                              @RequestParam(value = "", required = false) String stopTime){
        Map<String, Object> map = new HashMap<>();
        //System.out.println(startTime);
        map.put("topics", iWeiboService.getTopicNum(startTime, stopTime));
        return map;
    }

    @ResponseBody
    @RequestMapping("/getOneTopicMessage")
    public Map getOneTopicNumInfo(
                            String title,
                            @RequestParam(value = "", required = false) String startTime,
                            @RequestParam(value = "", required = false) String stopTime){
        Map<String, Object> map = new HashMap<>();
        //System.out.println(startTime);
        map.put("oneTitle", iWeiboService.getOneTopicNumInfo(title, startTime, stopTime));
        return map;
    }

    /**
     * 单个话题的转发 评论 点赞数
     */
    @RequestMapping("/gotoTopicInfo")
    public String topicInfo(String title, Model model){
        model.addAttribute("weibos", iWeiboService.getTopicInfo(title));
        model.addAttribute("title", title);
        return "topic_info";
    }

    @ResponseBody
    @RequestMapping("/gotoTopics")
    public Map<String, Object> topics(String title){
        Map<String, Object> map = new HashMap<>();
        map.put("weibos", iWeiboService.getTopicInfo(title));
        map.put("title", title);
        return map;
    }

    /**
     * 所有话题的转发 评论 点赞数
     */
    @RequestMapping("/goto3DSurface")
    public String topicInfo2(Model model){
        model.addAttribute("weibos", iWeiboService.getTopicInfo(""));
        return "topic_info2";
    }

    /**
     * 所有话题的转发 评论 点赞数
     */
    @RequestMapping("/gotoScatter3D")
    public String scatter3D(Model model){
        model.addAttribute("weibos", iWeiboService.getTopicInfo(""));
        return "scatter3D";
    }

    /*--------------------------------分隔线(评论男女 是否认证统计)----------------------------------------*/

    /**
     * 评论男女 是否认证统计
     */
    @RequestMapping("/gotoCommentUser")
    public String commentUser(String title, Model model){
        model.addAttribute("title", title);
        return "comment_user";
    }
    @ResponseBody
    @RequestMapping("/getCommentUser")
    public Map<String, Object> getCommentUser(String title){
        Map<String, Object> map = new HashMap<>();
        map.put("commentUser", iCommentUser.selectByTitle(title));
        //System.out.println(iCommentUser.selectByTitle(title));
        return map;
    }

    /**
     * 跳转到评论详情页面
     */
    @RequestMapping("/gotocommentinfo")
    public String gotoCommentInfo(String messageId, Model model){
        model.addAttribute("commnetInfoList", iCommentService.selectByMessageId(messageId));
        return "commentInfo";
    }

    @ResponseBody
    @RequestMapping("/selectUserInfo")
    public Map<String, Object> gotoCommentInfo(String uid){
        Map<String, Object> map = new HashMap<>();
        map.put("user", iUserInfoService.selectByUId(uid));
        return map;
    }
    
    // 数据总览echarts
    @RequestMapping("/gotoMyEcharts")
    public String gotoMyEcharts(String title, Model model){
        Map<String, Object> map = new HashMap<>();
        System.out.println(title);
        model.addAttribute("title", title);
        return "echarts";
    }
}

