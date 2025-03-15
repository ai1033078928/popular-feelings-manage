package com.hrbu.utils

import java.util

import org.apache.hadoop.hbase.{Cell, CellUtil, CompareOperator}
import org.apache.hadoop.hbase.filter.{Filter, SingleColumnValueFilter}
import org.apache.hadoop.hbase.util.Bytes
import org.junit.{After, AfterClass, Assert, Before, BeforeClass, Test}

class HBaseUtilsTest {
  var hbaseUtils:HBaseUtils = null

  @Before
  def init(): Unit ={
    hbaseUtils = HBaseUtils.getInstance()
    System.out.println("Before")
  }

  @Test
  def testConnection(): Unit ={
    Assert.assertNotNull(hbaseUtils.getConnection)
  }

  @Test
  def testAdmin(): Unit ={
    Assert.assertNotNull(hbaseUtils.getAdmin)
  }

  @Test
  def testScanTable(): Unit = {
    System.out.println(hbaseUtils.scanAllRecord("bigdata:word_index"))
    //System.out.println(hbaseUtils.scanAllRecord("bigdata:wordcloud"))
    System.out.println(hbaseUtils.selectRow("bigdata:wordcloud", "274715214782423040"))
  }
  @Test
  def scanByTitle(): Unit ={
    // (单值过滤)过滤出列值对应的行
    val filter = new SingleColumnValueFilter(Bytes.toBytes("info"), Bytes.toBytes("title"), CompareOperator.EQUAL, Bytes.toBytes("$NANA将翻拍成国产电视剧"))
    val cells: util.List[Cell] = hbaseUtils.scanByFilter("bigdata:word_index", filter)
    val rowkey: String = Bytes.toString(CellUtil.cloneRow(cells.get(0)))
    println(hbaseUtils.selectRow("bigdata:wordcloud", rowkey))
  }

  //@Test
  def testCreateNameSpace(): Unit ={
    hbaseUtils.createNameSpace("bigdata")
  }

  //@AfterClass // 释放资源 对于每一个测试方法都要执行一次 （注意与AfterClass区别，后者是对于所有方法执行一次）
  def close(): Unit ={
    hbaseUtils.getConnection.close()
    System.out.println("After")
  }
}
