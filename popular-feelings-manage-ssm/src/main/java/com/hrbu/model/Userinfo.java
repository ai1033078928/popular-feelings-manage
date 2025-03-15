package com.hrbu.model;

public class Userinfo {
    /**
    * 用户id
    */
    private String uid;

    /**
    * 用户昵称
    */
    private String name;

    /**
    * 用户描述
    */
    private String udesc;

    /**
    * 关注数
    */
    private Integer followcount;

    /**
    * 粉丝数
    */
    private Integer followerscount;

    /**
    * 性别
    */
    private String gender;

    /**
    * 是否认证
    */
    private Boolean verified;

    /**
    * 认证原因
    */
    private String verifiedreason;

    /**
    * 用户主页
    */
    private String uurl;

    /**
    * 地址
    */
    private String location;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUdesc() {
        return udesc;
    }

    public void setUdesc(String udesc) {
        this.udesc = udesc;
    }

    public Integer getFollowcount() {
        return followcount;
    }

    public void setFollowcount(Integer followcount) {
        this.followcount = followcount;
    }

    public Integer getFollowerscount() {
        return followerscount;
    }

    public void setFollowerscount(Integer followerscount) {
        this.followerscount = followerscount;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public String getVerifiedreason() {
        return verifiedreason;
    }

    public void setVerifiedreason(String verifiedreason) {
        this.verifiedreason = verifiedreason;
    }

    public String getUurl() {
        return uurl;
    }

    public void setUurl(String uurl) {
        this.uurl = uurl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}