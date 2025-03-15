package com.hrbu.model;

public class Wordcloud {
    /**
    * 标题
    */
    private String title;

    /**
    * 词
    */
    private String word;

    /**
    * 数量
    */
    private Integer number;

    public Wordcloud() {
    }

    public Wordcloud(String title, String word, Integer number) {
        this.title = title;
        this.word = word;
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}