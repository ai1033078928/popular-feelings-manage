package com.hrbu.model;

public class Topn {
    private int id;

    private Weibo weibo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Weibo getWeibo() {
        return weibo;
    }

    public void setWeibo(Weibo weibo) {
        this.weibo = weibo;
    }

    @Override
    public String toString() {
        return "Topn{" +
                "id=" + id +
                ", weibo=" + weibo +
                '}';
    }
}