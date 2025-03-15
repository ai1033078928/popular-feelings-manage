package com.hrbu.model;

public class Weibo {
    /**
     * 微博id
     */
    private String messageid;

    /**
     * 正向感情占比
     */
    private Double positive;

    /**
     * 标题
     */
    private String title;

    /**
     * 微博创建时间
     */
    private String createtime;

    /**
     * 创建微博用户名
     */
    private String username;

    /**
     * 微博链接
     */
    private String messageurl;

    /**
     * 微博正文
     */
    private String text;

    /**
     * 转发数
     */
    private Integer repostscount;

    /**
     * 评论数
     */
    private Integer commentscount;

    /**
     * 点赞数
     */
    private Integer attitudescount;

    /**
     * 微博正文感情判定
     */
    private String weiboemotion;

    public String getMessageid() {
        return messageid;
    }

    public void setMessageid(String messageid) {
        this.messageid = messageid;
    }

    public Double getPositive() {
        return positive;
    }

    public void setPositive(Double positive) {
        this.positive = positive;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessageurl() {
        return messageurl;
    }

    public void setMessageurl(String messageurl) {
        this.messageurl = messageurl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getRepostscount() {
        return repostscount;
    }

    public void setRepostscount(Integer repostscount) {
        this.repostscount = repostscount;
    }

    public Integer getCommentscount() {
        return commentscount;
    }

    public void setCommentscount(Integer commentscount) {
        this.commentscount = commentscount;
    }

    public Integer getAttitudescount() {
        return attitudescount;
    }

    public void setAttitudescount(Integer attitudescount) {
        this.attitudescount = attitudescount;
    }

    public String getWeiboemotion() {
        return weiboemotion;
    }

    public void setWeiboemotion(String weiboemotion) {
        this.weiboemotion = weiboemotion;
    }
}