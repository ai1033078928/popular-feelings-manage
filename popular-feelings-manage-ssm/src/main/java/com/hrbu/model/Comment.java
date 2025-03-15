package com.hrbu.model;

public class Comment {
    /**
    * 微博id
    */
    private String messageid;

    /**
    * 评论感情
    */
    private String commentemotion;

    /**
    * 评论正文
    */
    private String commentbody;

    /**
    * 评论用户id
    */
    private String uid;

    public String getMessageid() {
        return messageid;
    }

    public void setMessageid(String messageid) {
        this.messageid = messageid;
    }

    public String getCommentemotion() {
        return commentemotion;
    }

    public void setCommentemotion(String commentemotion) {
        this.commentemotion = commentemotion;
    }

    public String getCommentbody() {
        return commentbody;
    }

    public void setCommentbody(String commentbody) {
        this.commentbody = commentbody;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}