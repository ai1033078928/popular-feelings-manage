package com.hrbu

import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Put}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor, TableName}
import org.apache.hadoop.mapred.JobConf
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
 * @describe 把三个文件内容导入hbase
 * @time 20200610
 * @version 0.1
 */



/**
 * hbase连接配置
 */
case class HbaseConfig( hosts: String, port: String )

object DataLoder {
  // 定义数据存储路径
  val WEIBO_DATA_PATH = "/datas/微博.csv"
  val COMMENT_DATA_PATH = "/datas/微博评论.csv"
  val USERINFO_DATA_PATH = "/datas/微博个人信息.csv"

  // 定义hbase存储表名
  val HBASE_WEIBO_TABLE = "std:weibo"
  val HBASE_COMMENT_TABLE = "std:comment"
  val HBASE_USERINFO_TABLE = "std:userinfo"

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "hbase.zookeeper.quorum" -> "hadoop1",
      "hbase.zookeeper.property.clientPort" -> "2181",
    )

    // 创建spark config
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("DataLoader")

    // 创建spark session
    val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    // 加载数据
    val weiboRDD: RDD[String] = spark.sparkContext.textFile(WEIBO_DATA_PATH)

    val commmentRDD: RDD[String] = spark.sparkContext.textFile(COMMENT_DATA_PATH)

    val userInfoRDD: RDD[String] = spark.sparkContext.textFile(USERINFO_DATA_PATH)

    // 隐式参数
    implicit val hbaseConfig = HbaseConfig( config("hbase.zookeeper.quorum"), config("hbase.zookeeper.property.clientPort") )

    // 向hbase存入weiboRDD
    storeWeiboToHbase(weiboRDD)
    // 向hbase存入commentRDD
    storeCommentToHbase(commmentRDD)
    // 向hbase存入userInfoRDD
    storeUserInfoToHbase(userInfoRDD)

    spark.stop()
  }

  // 存储到hbase 隐式参数的好处是多次调用时不用每次都传进去
  def storeWeiboToHbase( weiboRDD: RDD[String] )(implicit hbaseConfig: HbaseConfig): Unit ={

    // 初始化jobconf，TableOutputFormat必须是org.apache.hadoop.hbase.mapred包下的
    val hbaseConf = HBaseConfiguration.create()
    hbaseConf.set("hbase.zookeeper.quorum", hbaseConfig.hosts)  //设置zooKeeper集群地址，也可以通过将hbase-site.xml导入classpath，但是建议在程序里这样设置
    hbaseConf.set("hbase.zookeeper.property.clientPort", hbaseConfig.port)       //设置zookeeper连接端口，默认2181
    hbaseConf.set(TableOutputFormat.OUTPUT_TABLE, HBASE_WEIBO_TABLE)

    // 初始化job，TableOutputFormat 是 org.apache.hadoop.hbase.mapred 包下的
    val jobConf = new JobConf(hbaseConf)
    jobConf.setOutputFormat(classOf[TableOutputFormat])

    // 构建HBase表描述器
    val wTable: TableName = TableName.valueOf(HBASE_WEIBO_TABLE)
    val wTableDesc = new HTableDescriptor(wTable)
    wTableDesc.addFamily(new HColumnDescriptor("info".getBytes))

    // 创建Hbase表
    val connection: Connection = ConnectionFactory.createConnection(hbaseConf)
    val admin = connection.getAdmin
    if (admin.tableExists(wTable)) {
      admin.disableTable(wTable)
      admin.deleteTable(wTable)
    }
    admin.createTable(wTableDesc)

    val rdd = weiboRDD.map(_.split('\t')).map {
      arr => {
        // 设置行健的值
        val put = new Put(Bytes.toBytes(arr(1)))
        //设置info列的值
        // title, messageId, createTime, userName,messageUrl, text, repostsCount, commentsCount, attitudesCount
        // attr(0).trim, attr(1).toInt, attr(6).trim, attr(8).trim, attr(9).trim, attr(10).trim, attr(11).toInt, attr(12).toInt, attr(13).toInt
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("title"), Bytes.toBytes(arr(0)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("messageId"), Bytes.toBytes(arr(1)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("createTime"), Bytes.toBytes(arr(6)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("userName"), Bytes.toBytes(arr(8)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("messageUrl"), Bytes.toBytes(arr(9)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("text"), Bytes.toBytes(arr(10)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("repostsCount"), Bytes.toBytes(arr(11)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("commentsCount"), Bytes.toBytes(arr(12)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("attitudesCount"), Bytes.toBytes(arr(13)))
        // 设置info:gender列的值
        // put.addColumn(Bytes.toBytes("info"),Bytes.toBytes("233"), Bytes.toBytes(arr(1)))
        // 构建一个键值对，作为rdd的一个元素
        //转化成RDD[(ImmutableBytesWritable,Put)]类型才能调用saveAsHadoopDataset
        (new ImmutableBytesWritable, put)
      }
    }

    rdd.saveAsHadoopDataset(jobConf)

    admin.close()
    connection.close()

  }

  def storeCommentToHbase( commmentRDD: RDD[String] )(implicit hbaseConfig: HbaseConfig): Unit ={
    // 初始化jobconf，TableOutputFormat必须是org.apache.hadoop.hbase.mapred包下的
    val hbaseConf = HBaseConfiguration.create()
    hbaseConf.set("hbase.zookeeper.quorum", hbaseConfig.hosts)  //设置zooKeeper集群地址，也可以通过将hbase-site.xml导入classpath，但是建议在程序里这样设置
    hbaseConf.set("hbase.zookeeper.property.clientPort", hbaseConfig.port)       //设置zookeeper连接端口，默认2181
    hbaseConf.set(TableOutputFormat.OUTPUT_TABLE, HBASE_COMMENT_TABLE)

    // 初始化job，TableOutputFormat 是 org.apache.hadoop.hbase.mapred 包下的
    val jobConf = new JobConf(hbaseConf)
    jobConf.setOutputFormat(classOf[TableOutputFormat])

    // 构建HBase表描述器
    val wTable: TableName = TableName.valueOf(HBASE_COMMENT_TABLE)
    val wTableDesc = new HTableDescriptor(wTable)
    wTableDesc.addFamily(new HColumnDescriptor("info".getBytes))

    // 创建Hbase表
    val connection: Connection = ConnectionFactory.createConnection(hbaseConf)
    val admin = connection.getAdmin
    if (admin.tableExists(wTable)) {
      admin.disableTable(wTable)
      admin.deleteTable(wTable)
    }
    admin.createTable(wTableDesc)

    val rdd = commmentRDD.map(_.split('\t')).map {
      arr => {
        // 设置行健的值
        val put = new Put(Bytes.toBytes(arr(1) + arr(3)))
        //设置info列的值
        // title, messageId, text, uid
        // Comment(attr(0).trim, attr(1).toInt, attr(2).trim, attr(3).toInt)
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("title"), Bytes.toBytes(arr(0)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("messageId"), Bytes.toBytes(arr(1)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("text"), Bytes.toBytes(arr(2)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("uid"), Bytes.toBytes(arr(3)))

        //转化成RDD[(ImmutableBytesWritable,Put)]类型才能调用saveAsHadoopDataset
        (new ImmutableBytesWritable, put)
      }
    }

    rdd.saveAsHadoopDataset(jobConf)

    admin.close()
    connection.close()
  }

  def storeUserInfoToHbase( userInfoRDD: RDD[String] )(implicit hbaseConfig: HbaseConfig): Unit ={
    // 初始化jobconf，TableOutputFormat必须是org.apache.hadoop.hbase.mapred包下的
    val hbaseConf = HBaseConfiguration.create()
    hbaseConf.set("hbase.zookeeper.quorum", hbaseConfig.hosts)  //设置zooKeeper集群地址，也可以通过将hbase-site.xml导入classpath，但是建议在程序里这样设置
    hbaseConf.set("hbase.zookeeper.property.clientPort", hbaseConfig.port)       //设置zookeeper连接端口，默认2181
    hbaseConf.set(TableOutputFormat.OUTPUT_TABLE, HBASE_USERINFO_TABLE)

    // 初始化job，TableOutputFormat 是 org.apache.hadoop.hbase.mapred 包下的
    val jobConf = new JobConf(hbaseConf)
    jobConf.setOutputFormat(classOf[TableOutputFormat])

    // 构建HBase表描述器
    val wTable: TableName = TableName.valueOf(HBASE_USERINFO_TABLE)
    val wTableDesc = new HTableDescriptor(wTable)
    wTableDesc.addFamily(new HColumnDescriptor("info".getBytes))

    // 创建Hbase表
    val connection: Connection = ConnectionFactory.createConnection(hbaseConf)
    val admin = connection.getAdmin
    if (admin.tableExists(wTable)) {
      admin.disableTable(wTable)
      admin.deleteTable(wTable)
    }
    admin.createTable(wTableDesc)

    val rdd = userInfoRDD.map(_.split('\t')).map {
      arr => {
        // 设置行健的值
        val put = new Put(Bytes.toBytes(arr(0)))
        //uid, name, uDesc, followCount, followersCount, gender, verified, verifiedReason, uUrl, location
        //UserInfo(attr(0).toInt, attr(1).trim, attr(2).trim, attr(3).toInt, attr(4).toInt, attr(5).trim, attr(6).toBoolean, attr(7).trim, attr(8).trim, attr(9).trim)
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("uid"), Bytes.toBytes(arr(0)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("name"), Bytes.toBytes(arr(1)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("uDesc"), Bytes.toBytes(arr(2)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("followCount"), Bytes.toBytes(arr(3)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("followersCount"), Bytes.toBytes(arr(4)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("gender"), Bytes.toBytes(arr(5)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("verified"), Bytes.toBytes(arr(6)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("verifiedReason"), Bytes.toBytes(arr(7)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("uUrl"), Bytes.toBytes(arr(8)))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("location"), Bytes.toBytes(arr(9)))
        //转化成RDD[(ImmutableBytesWritable,Put)]类型才能调用saveAsHadoopDataset
        (new ImmutableBytesWritable, put)
      }
    }

    rdd.saveAsHadoopDataset(jobConf)

    admin.close()
    connection.close()
  }

}
