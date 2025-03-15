package com.hrbu.service.impl;

import com.hrbu.util.HBaseUtils;
import com.hrbu.model.Wordcloud;
import com.hrbu.service.IHBaseService;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class HBaseServiceImpl implements IHBaseService {

    HBaseUtils hBaseUtils = HBaseUtils.getInstance();

    @Override
    public List<Wordcloud> selectWordCloud(String title) {
        List<Wordcloud> list = new ArrayList<>();

        // (单值过滤)过滤出列值对应的行
        SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes.toBytes("info"), Bytes.toBytes("title"), CompareOperator.EQUAL, Bytes.toBytes(title));
        try {
            List<Cell> cells = hBaseUtils.scanByFilter("bigdata:word_index", filter);
            String rowKey = Bytes.toString(CellUtil.cloneRow(cells.get(0)));
            Table table = hBaseUtils.getConnection().getTable(TableName.valueOf("bigdata:wordcloud"));

            Result result = table.get(new Get(rowKey.getBytes()));

            /**
             * 原因: 因为放入的时候是Long类型长度为8, 取出时为Int长度为4
             * 1.CellUtil.cloneQualifier(tempCell)抛异常IllegalArgumentException
             * 2.用cell.getQualifierArray乱码
             */
            for (Cell cell : result.rawCells()) {
                list.add( new Wordcloud(title,
                        Bytes.toString(CellUtil.cloneQualifier(cell)),
                        Integer.valueOf(Bytes.toString(CellUtil.cloneValue(cell)))) );
            }

        } catch (IOException e) {
            System.out.println("HBase 表不存在 (IO异常)");
            //e.printStackTrace();
        } catch (Exception e) {     // IllegalArgumentException
            System.out.println("Exception异常");
            e.printStackTrace();
        } finally {
            // System.out.println(list);
            return list;
        }

    }
}
