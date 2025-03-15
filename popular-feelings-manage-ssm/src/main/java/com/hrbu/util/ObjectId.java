package com.hrbu.util;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 唯一id,参考mango ObjectId
 * @author: aoliao
 * @updateTime: 2019/10/24 21:37
 */
public class ObjectId {

    private static AtomicInteger count = new AtomicInteger();
    /**
     * 生成16进制时间戳
     * @author: aoliao
     * @updateTime: 2019/10/24 21:17
     * @return :String
     */
    private static String getTimestamp(){
        long time = System.currentTimeMillis();
        //返回16进制时间戳
        return Long.toHexString(time);
    }
    /**
     * 解码时间戳
     * @param :String
     * @author: aoliao
     * @updateTime: 2019/10/24 21:24
     * @return :String
     */
    private static String decodeTimestamp(String timeStamp){
        long time = Long.valueOf(timeStamp,16);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //转换成时间
        return sdf.format(new Date(Long.parseLong(String.valueOf(time))));
    }
    /**
     * 获取16位pid
     * @author: aoliao
     * @updateTime: 2019/10/24 21:31
     * @return : String
     */
    private static String getPid(){
        String name = ManagementFactory.getRuntimeMXBean().getName();
        //获取pid
        String pid = name.split("@")[0];
        //转换成long
        long pidvalue = Long.valueOf(pid,10);
        //pid 16进制
        return Long.toHexString(pidvalue);
    }
    /**
     * @author: aoliao
     * @updateTime: 2019/10/24 21:33
     * @return :String
     */
    private static String inc(){
        //自增数
        return String.valueOf(count.getAndIncrement());
    }
    public static String getId(){
        String timestamp = getTimestamp();
        String pid = getPid();
        String inc =inc();
        return timestamp + pid +inc;
    }

    public static String getData(String id){
        return decodeTimestamp(id.substring(0,11));
    }
    /**
     * 数字id
     * @author: aoliao
     * @updateTime: 2019/11/2 15:59
     */
    public static String numberId(){
        long time = System.currentTimeMillis();
        String inc = inc();
        String numberId = String.valueOf(time) + inc;
        if (numberId.length()>10){
            double d = Math.random();
            int i = (int)(d*1000);
            return numberId.substring(8,13)+String.valueOf(i);
        }
        return numberId;
    }
}
