package com.hrbu.model;

public class CommentUser {
    /**
    * 标题
    */
    private String title;

    /**
    * 评论用户总数
    */
    private Integer total;

    /**
    * 男生数
    */
    private Integer male;

    /**
    * 女生数
    */
    private Integer female;

    /**
    * 认证数
    */
    private Integer verified;

    /**
    * 未认证数
    */
    private Integer unverified;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getMale() {
        return male;
    }

    public void setMale(Integer male) {
        this.male = male;
    }

    public Integer getFemale() {
        return female;
    }

    public void setFemale(Integer female) {
        this.female = female;
    }

    public Integer getVerified() {
        return verified;
    }

    public void setVerified(Integer verified) {
        this.verified = verified;
    }

    public Integer getUnverified() {
        return unverified;
    }

    public void setUnverified(Integer unverified) {
        this.unverified = unverified;
    }

    @Override
    public String toString() {
        return "CommentUser{" +
                "title='" + title + '\'' +
                ", total=" + total +
                ", male=" + male +
                ", female=" + female +
                ", verified=" + verified +
                ", unverified=" + unverified +
                '}';
    }
}