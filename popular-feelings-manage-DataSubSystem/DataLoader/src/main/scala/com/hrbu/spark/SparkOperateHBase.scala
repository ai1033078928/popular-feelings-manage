package com.hrbu.spark

import org.apache.hadoop.hbase.{HBaseConfiguration, HTableDescriptor, TableName}
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Result}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession


object SparkOperateHBase {
  def main(args: Array[String]) {

    val spark: SparkSession = SparkSession.builder()
      .appName("SparkHBaseRDD")
      .master("local")
      .getOrCreate()

    val sc: SparkContext = spark.sparkContext

    val tablename = "std:weibo"

    val hbaseConf = HBaseConfiguration.create()
    hbaseConf.set("hbase.zookeeper.quorum", "hadoop1") //设置zooKeeper集群地址，也可以通过将hbase-site.xml导入classpath，但是建议在程序里这样设置
    hbaseConf.set("hbase.zookeeper.property.clientPort", "2181") //设置zookeeper连接端口，默认2181
    hbaseConf.set(TableInputFormat.INPUT_TABLE, tablename)

    // 如果表不存在，则创建表
    val connection: Connection = ConnectionFactory.createConnection(hbaseConf)
    val admin = connection.getAdmin()
    if (!admin.tableExists(TableName.valueOf(tablename))) {
      // val tableDesc = new HTableDescriptor(TableName.valueOf(tablename))
      // admin.createTable(tableDesc)
      println("表不存在")
    } else {

      //读取数据并转化成rdd TableInputFormat 是 org.apache.hadoop.hbase.mapreduce 包下的
      val hBaseRDD: RDD[(ImmutableBytesWritable, Result)] = sc.newAPIHadoopRDD(
        hbaseConf,
        classOf[TableInputFormat],
        classOf[ImmutableBytesWritable],
        classOf[Result]
      )

      hBaseRDD.map{
        case (_, result) =>
          val key = Bytes.toString(result.getRow)
          val title = Bytes.toString(result.getValue("info".getBytes, "title".getBytes))
          val messageId = Bytes.toString(result.getValue("info".getBytes, "messageId".getBytes))
          (key, title, messageId)
      }.takeOrdered(10)
        .foreach(println(_))


      /*
      hBaseRDD.foreach {
        case (_, result) =>
          //获取行键
          val key = Bytes.toString(result.getRow)
          //通过列族和列名获取列
          val title = Bytes.toString(result.getValue("info".getBytes, "title".getBytes))
          val messageId = Bytes.toString(result.getValue("info".getBytes, "messageId".getBytes))
          val createTime = Bytes.toString(result.getValue("info".getBytes, "createTime".getBytes))
          val userName = Bytes.toString(result.getValue("info".getBytes, "userName".getBytes))
          val messageUrl = Bytes.toString(result.getValue("info".getBytes, "messageUrl".getBytes))
          val text = Bytes.toString(result.getValue("info".getBytes, "text".getBytes))
          val repostsCount = Bytes.toString(result.getValue("info".getBytes, "repostsCount".getBytes))
          val commentsCount = Bytes.toString(result.getValue("info".getBytes, "commentsCount".getBytes))
          val attitudesCount = Bytes.toString(result.getValue("info".getBytes, "attitudesCount".getBytes))
          // val age = Bytes.toString(result.getValue("info".getBytes, "233".getBytes))
          println(f"Row key: ${key}\n${title}\t${messageId}\t${createTime}\t${userName}\t${messageUrl}\t${text}\t${repostsCount}\t${commentsCount}\t${attitudesCount}\t")
      }
      */
    }
    admin.close()
    connection.close()
    spark.stop()
  }
}
