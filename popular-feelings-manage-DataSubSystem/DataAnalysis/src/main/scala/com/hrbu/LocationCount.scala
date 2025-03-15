package com.hrbu

import com.hrbu.config.MyConfig
import com.hrbu.udf.UserInfoUDF
import com.hrbu.utils.{HdfsToSpark, SparkToMySQL}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.{functions => F}

/*运行(22:33-22:35)(22:36-22:39)(22:57-22:59)*/
/**
 * 评价数据
 */
case class Comment(rowkey: String, title: String, messageId: String, text: String, uid: String)

/**
 * 评价用户个人信息
 */
case class UserInfo(uid: String, name: String, uDesc: String, followCount: Int, followersCount: Int, gender: String,
                    verified: Boolean, verifiedReason: String, uUrl: String, location: String)

/**
 * @DO 统计每个主题  评论者的位置 并存入MySQL数据库
 *
 * 省份编码 c11 c12 c13 c14 c15
 * 话题一  12  21  2   12  21
 * 话题二  56  86  21  53  36
 */
object CommentAnalysis {
  // 定义数据存储路径
  //val COMMENT_DATA_PATH = "D:\\software\\python\\jupyterFile\\data\\项目数据\\微博评论.csv"
  //val USERINFO_DATA_PATH = "D:\\software\\python\\jupyterFile\\data\\项目数据\\微博个人信息.csv"
  //val GET_MESSAGE = "$河南禁用一次性塑料制品"

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[4]",
      //"spark.cores" -> "spark://192.168.1.100:7077",
      "table.name" -> "location"
    )
    // 创建配置对象
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("TransfromData")
    // 创建SparkSession
    // 解决Truncated the string representation of a plan since it was too large. This behavior can be adjusted by setting 'spark.debug.maxToStringFields' in SparkEnv.conf.
    // 永久解决 在spark-defaults.conf文件中添加一行处理，重启slave,再此进入spark-shell, warning消失了。spark.debug.maxToStringFields=100
    val spark: SparkSession = SparkSession.builder().config(sparkConf).config("spark.debug.maxToStringFields", "100").getOrCreate()

    // 导入隐式转换
    import spark.implicits._

    /**
     * 从hdfs读取sequenceFile文件
     */
    // 加载数据 (注: HdfsToSpark是定义的工具类) 本地spark.sparkContext.textFile(COMMENT_DATA_PATH)
    val commentDF: DataFrame = HdfsToSpark.getSequenceFileFromFolder(spark.sparkContext, MyConfig.COMMENT_DATA_PATH)
      .map(
        line => {
          val attr: Array[String] = line.split("\t")
          // Weibo( str(0), str(1), str(6), str(8), str(9), str(10), str(11).toInt,str(12).toInt, str(13).toInt )
          // UserInfo(attr(0).toInt, attr(1).trim, attr(2).trim, attr(3).toInt, attr(4).toInt, attr(5).trim, attr(6).toBoolean, attr(7).trim, attr(8).trim, attr(9).trim)
          Comment( (attr(1)+attr(3)).trim, attr(0).trim, attr(1).trim, attr(2).trim, attr(3).trim )
        }
      ).toDF()

    // 加载数据 (注: HdfsToSpark是定义的工具类) 本地spark.sparkContext.textFile(Config.USERINFO_DATA_PATH)
    val userinfoDF: DataFrame = HdfsToSpark.getSequenceFileFromFolder(spark.sparkContext, MyConfig.USERINFO_DATA_PATH)
      .map(
        line => {
          val attr: Array[String] = line.split("\t")
          UserInfo( attr(0).trim, attr(1).trim, attr(2).trim, attr(3).toInt, attr(4).toInt, attr(5).trim, attr(6).toBoolean, attr(7).trim, attr(8).trim, attr(9).trim )
        }
      ).toDF()

    // 加入filter后, 会优化join, 先执行过滤
    val joinDF: DataFrame = commentDF.join(userinfoDF, Seq("uid"), "inner") //commentDF("uid") === userinfoDF("uid")
    //  .filter(commentDF("title") === GET_MESSAGE)  // 这里返回值是DataSet
    //.filter(userinfoDF("followersCount") > 100000)

    // 创建临时视图
    joinDF.createOrReplaceTempView("join_table")

    // 自定义函数  把省份映射为 数字
    spark.udf.register("transformlocation", UserInfoUDF.transformationOfLocation(_))

    val loactionDF: DataFrame = spark.sql(
      """
        |SELECT title,transformlocation(location) as address,COUNT(location) as num
        |FROM join_table
        |GROUP BY title,location
      """.stripMargin)

    //loactionDF.show(100, false)

    /**
     * 行转列 透视（pivot）
     */
    // 省份编码
    val list = List(11,12,13,14,15,21,22,23,31,32,33,34,35,36,37,41,42,43,44,45,46,50,51,52,53,54,61,62,63,64,65,71,81,82,99,100)
    val locationRes: DataFrame = loactionDF   //.coalesce(1)   //重新分区
      .groupBy("title")
      .pivot("address", list)
      .agg(F.max("num"))
      .na.fill(0)
      .toDF("title","c_11","c_12","c_13","c_14","c_15","c_21","c_22","c_23","c_31","c_32","c_33","c_34","c_35","c_36","c_37","c_41","c_42","c_43","c_44","c_45","c_46","c_50","c_51","c_52","c_53","c_54","c_61","c_62","c_63","c_64","c_65","c_71","c_81","c_82","c_99","c_100")
      //.show(50,false)

    // 隐式参数 (注: SparkToMySQL是定义的工具类)
    implicit val mySQLConfig = SparkToMySQL.mySQLConfig
    SparkToMySQL.dataFrameToMysql(locationRes, config("table.name"))

    // 关闭资源
    spark.close()
  }
}

