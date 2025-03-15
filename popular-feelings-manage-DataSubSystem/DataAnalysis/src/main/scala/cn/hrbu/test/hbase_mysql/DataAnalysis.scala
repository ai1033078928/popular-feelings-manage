package cn.hrbu.test.hbase_mysql

import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Result}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * @info 把数据从hbase取出 (并进行操作)
 */

/**
 * hbase连接配置
 */
case class HbaseConfig(hosts: String, port: String)

/**
 * mysql连接配置
 */
case class MySQLConfig(url: String, user: String, password: String)

object DataAnalysis {

  // 定义hbase表名
  val HBASE_WEIBO_TABLE = "std:weibo"
  val HBASE_COMMENT_TABLE = "std:comment"
  val HBASE_USERINFO_TABLE = "std:userinfo"

  // 定义MySql表名
  val MYSQL_WEIBO_TABLE = "weibo"
  val MYSQL_COMMENT_TABLE = "comment"
  val MYSQL_USERINFO_TABLE = "userinfo"


  def main(args: Array[String]): Unit = {

    //java.sql.SQLException: Unsupported character encoding 'utf8mb4'
    val config = Map(
      "spark.cores" -> "local[*]",
      "hbase.zookeeper.quorum" -> "hadoop1",
      "hbase.zookeeper.property.clientPort" -> "2181",
      "mysql.url" -> "jdbc:mysql://localhost:3306/myweibo?serverTimezone=UTC&useUnicode=true",
      "mysql.user" -> "root",
      "mysql.password" -> "123456789"
    )

    // 创建spark配置
    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("DataReader")
    // 创建SparkSession
    val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    // 隐式参数.
    implicit val hbaseConfig = HbaseConfig(config("hbase.zookeeper.quorum"), config("hbase.zookeeper.property.clientPort"))
    implicit val mySQLConfig = MySQLConfig(config("mysql.url"), config("mysql.user"), config("mysql.password"))

    // 导入隐式转换

    // 获取hbase表数据
    //commentRDDToDF(spark, getHbaseRDD(spark, HBASE_COMMENT_TABLE)).take(10).foreach(println) // 查看10条数据
    userInfoRDDToDF(spark, getHbaseRDD(spark, HBASE_USERINFO_TABLE)).take(10).foreach(println) // 查看10条数据
    //val weiboDF: DataFrame = weiboRDDToDF(spark, getHbaseRDD(spark, HBASE_WEIBO_TABLE))
    val commentDF: DataFrame = commentRDDToDF(spark, getHbaseRDD(spark, HBASE_COMMENT_TABLE))
    //    val userInfoDF: DataFrame = userInfoRDDToDF(spark, getHbaseRDD(spark, HBASE_USERINFO_TABLE))

    // 存储到MySQL
    //dataFrameToMysql(weiboDF, MYSQL_WEIBO_TABLE)
    dataFrameToMysql(commentDF, MYSQL_COMMENT_TABLE)
    //    dataFrameToMysql(userInfoDF, MYSQL_USERINFO_TABLE)

    // 创建一张名叫weibo的临时表(临时视图)  生命周期与用于创建此数据集的[SparkSession]相关联
    //weiboDF.createOrReplaceTempView("weibo")

    // 创建全局临时视图，此时图的生命周期与Spark Application绑定
    //weiboDF.createGlobalTempView("weibo")

    //    val selectDF: DataFrame = spark.sql("select * from weibo where title='$闺蜜是拿来利用的吗'")
    //    selectDF.foreach(println(_))

    spark.close()

  }


  // 定义读取hbase的函数
  def getHbaseRDD(spark: SparkSession, tableName: String)(implicit hbaseConfig: HbaseConfig): RDD[(ImmutableBytesWritable, Result)] = {

    // 初始化jobconf
    val hbaseConf = HBaseConfiguration.create()
    hbaseConf.set("hbase.zookeeper.quorum", hbaseConfig.hosts) //设置zooKeeper集群地址，也可以通过将hbase-site.xml导入classpath，但是建议在程序里这样设置
    hbaseConf.set("hbase.zookeeper.property.clientPort", hbaseConfig.port) //设置zookeeper连接端口，默认2181
    hbaseConf.set(TableInputFormat.INPUT_TABLE, tableName)

    // 定义返回值变量
    var hBaseRDD: RDD[(ImmutableBytesWritable, Result)] = null

    // 如果表不存在，则创建表
    val connection: Connection = ConnectionFactory.createConnection(hbaseConf)
    val admin = connection.getAdmin()
    if (!admin.tableExists(TableName.valueOf(tableName))) {
      // val tableDesc = new HTableDescriptor(TableName.valueOf(tablename))
      // admin.createTable(tableDesc)
      println("表不存在")
    } else {
      //读取数据并转化成rdd TableInputFormat 是 org.apache.hadoop.hbase.mapreduce 包下的
      hBaseRDD = spark.sparkContext.newAPIHadoopRDD(
        hbaseConf,
        classOf[TableInputFormat],
        classOf[ImmutableBytesWritable],
        classOf[Result])
    }

    admin.close()
    connection.close()
    hBaseRDD
  }

  /**
   * 微博数据
   */
  case class Weibo(title: String, messageId: String, createTime: String, userName: String,
                   messageUrl: String, text: String, repostsCount: Int, commentsCount: Int, attitudesCount: Int)

  /**
   * 评价数据
   */
  case class Comment(rowkey: String, title: String, messageId: String, text: String, uid: String)

  /**
   * 评价用户个人信息
   */
  case class UserInfo(uid: String, name: String, uDesc: String, followCount: Int, followersCount: Int, gender: String,
                      verified: Boolean, verifiedReason: String, uUrl: String, location: String)


  def weiboRDDToDF(spark: SparkSession, rdd: RDD[(ImmutableBytesWritable, Result)]): DataFrame = {
    // 导入隐式转换
    import spark.implicits._

    val df: DataFrame = rdd.map(line => {
      Weibo(
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("title"))),
        Bytes.toString(line._2.getRow),
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("createTime"))),
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("userName"))),
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("messageUrl"))),
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("text"))),
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("repostsCount"))).toInt,
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("commentsCount"))).toInt,
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("attitudesCount"))).toInt,
      )
    }).toDF()

    df
  }

  def commentRDDToDF(spark: SparkSession, rdd: RDD[(ImmutableBytesWritable, Result)]): DataFrame = {
    // 导入隐式转换
    import spark.implicits._

    val df: DataFrame = rdd.map(line => {
      // title: String, messageId: String, text: String, uid: String
      Comment(
        Bytes.toString(line._2.getRow),
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("title"))),
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("messageId"))),
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("text"))),
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("uid"))),
      )
    }).toDF()

    df
  }

  def userInfoRDDToDF(spark: SparkSession, rdd: RDD[(ImmutableBytesWritable, Result)]): DataFrame = {
    // 导入隐式转换
    import spark.implicits._

    val df: DataFrame = rdd.map(line => {
      //uid: String, name: String, uDesc: String, followCount: Int, followersCount: Int,
      // gender: String, verified: Boolean, verifiedReason: String, uUrl: String, location: String
      UserInfo(
        Bytes.toString(line._2.getRow),
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("name"))),
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("uDesc"))),
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("followCount"))).toInt,
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("followersCount"))).toInt,
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("gender"))),
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("verified"))).toBoolean,
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("verifiedReason"))),
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("uUrl"))),
        Bytes.toString(line._2.getValue(Bytes.toBytes("info"), Bytes.toBytes("location")))
      )
    }).toDF()

    df
  }


  def dataFrameToMysql(df: DataFrame, dbtable: String)(implicit mySQLConfig: MySQLConfig): Unit = {
    /**
     * 真的是存不下???
     * com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Data too long for column 'uDesc' at row 1
     */
    df.write.mode("append")
      .format("jdbc")
      .option("url", mySQLConfig.url)
      .option("dbtable", dbtable)
      .option("user", mySQLConfig.user)
      .option("password", mySQLConfig.password)
      .option("driver", "com.mysql.cj.jdbc.Driver")
      .option("batchsize", "50000")
      .option("truncate", "true")
      .save()
  }
}