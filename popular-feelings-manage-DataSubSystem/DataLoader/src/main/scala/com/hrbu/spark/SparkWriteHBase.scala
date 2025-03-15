package com.hrbu.spark


import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.mapred.JobConf
import org.apache.spark.sql.SparkSession


object SparkWriteHBase {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("SparkHBaseRDD")
      .master("local")
      .getOrCreate()
    val sc = spark.sparkContext

    // 表名
    val tablename = "std:userInfo"

    // 初始化jobconf，TableOutputFormat必须是org.apache.hadoop.hbase.mapred包下的
    val hbaseConf = HBaseConfiguration.create()
    hbaseConf.set("hbase.zookeeper.quorum","hadoop1")  //设置zooKeeper集群地址，也可以通过将hbase-site.xml导入classpath，但是建议在程序里这样设置
    hbaseConf.set("hbase.zookeeper.property.clientPort", "2181")       //设置zookeeper连接端口，默认2181
    hbaseConf.set(TableOutputFormat.OUTPUT_TABLE, tablename)

    // 初始化job，TableOutputFormat 是 org.apache.hadoop.hbase.mapred 包下的
    val jobConf = new JobConf(hbaseConf)
    jobConf.setOutputFormat(classOf[TableOutputFormat])

    //下面这行代码用于构建两行记录
    val indataRDD = sc.makeRDD(
      Array(
        "3939254772,Mama宁",
        "1406309052,轩辕无天"
      )
    )

    val rdd = indataRDD.map(_.split(',')).map {
      arr => {
        // 设置行健的值
        val put = new Put(Bytes.toBytes(arr(0)))
        //设置info:name列的值
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("name"), Bytes.toBytes(arr(1)))
        // 设置info:gender列的值
        // put.addColumn(Bytes.toBytes("info"),Bytes.toBytes("233"), Bytes.toBytes(arr(1)))
        // 构建一个键值对，作为rdd的一个元素
        //转化成RDD[(ImmutableBytesWritable,Put)]类型才能调用saveAsHadoopDataset
        (new ImmutableBytesWritable, put)
      }
    }

    rdd.saveAsHadoopDataset(jobConf)

    sc.stop()
  }
}
