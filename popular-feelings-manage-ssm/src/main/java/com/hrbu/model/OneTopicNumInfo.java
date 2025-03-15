package com.hrbu.model;

public class OneTopicNumInfo {

    private String title;

    private String createTime;

    private Integer num;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "OneTopicNumInfo{" +
                "title='" + title + '\'' +
                ", createTime='" + createTime + '\'' +
                ", num=" + num +
                '}';
    }
}
