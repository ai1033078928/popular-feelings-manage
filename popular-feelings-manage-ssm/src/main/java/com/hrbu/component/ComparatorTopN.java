package com.hrbu.component;

import com.hrbu.model.Topn;

import java.util.Comparator;

/**
 * 微博排行比较器
 */
public class ComparatorTopN implements Comparator<Topn> {
    @Override
    public int compare(Topn o1, Topn o2) {
        if (o1.getId() > o2.getId()){
            return 1;
        } else if (o1.getId() < o2.getId()){
            return -1;
        } else {
            return 0;
        }

    }
}
