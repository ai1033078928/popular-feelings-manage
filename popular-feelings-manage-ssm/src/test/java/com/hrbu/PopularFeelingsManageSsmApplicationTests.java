package com.hrbu;

import com.hrbu.controller.DataController;
import com.hrbu.model.Weibo;
import com.hrbu.service.impl.WeiboServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.xml.crypto.Data;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SpringBootTest
class PopularFeelingsManageSsmApplicationTests {

    @Test
    void contextLoads() {
        WeiboServiceImpl weiboService = new WeiboServiceImpl();

        List<Weibo> oneTopicNumInfo = weiboService.getOneTopicNumInfo("$高考", "2020-01-01", "2020-09-09");
        System.out.println(oneTopicNumInfo.get(0).toString());
    }
    @Test
    void test(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println(df.format(new Date()));
    }

}
