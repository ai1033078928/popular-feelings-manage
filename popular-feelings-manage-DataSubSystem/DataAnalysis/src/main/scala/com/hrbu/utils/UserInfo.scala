package com.hrbu.utils

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

object UserInfo {
  val USERINFO_PATH = "D:\\software\\python\\jupyterFile\\data\\项目数据\\微博个人信息2.csv"
  def main(args: Array[String]): Unit = {
    // 创建配置对象
    val sparkConf: SparkConf = new SparkConf().setAppName("UserInfo").setMaster("local[4]")
    // 创建spark对象
    val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    // 导入隐式转换
    import spark.implicits._

    val dataFrame: DataFrame = spark.sparkContext.textFile(USERINFO_PATH)
      .map(line => {
        val strings: Array[String] = line.split("\t")
        (strings(0), strings(6), strings(7))
      })
      .groupBy(_._1)    // 按title分组
      .map {            // 统计信息
        case (title, iterable) => {
          var total, male, female, verified, unverified = 0
          for (elem <- iterable) {
            total += 1
            if ("f".equalsIgnoreCase(elem._2)) female += 1 else if ("m".equalsIgnoreCase(elem._2)) male += 1
            if ("true".equalsIgnoreCase(elem._3)) verified += 1 else if ("false".equalsIgnoreCase(elem._3)) unverified += 1
          }
          (title, total, male, female, verified, unverified)
        }
      }
      //.toDF("标题", "评论用户id", "评论用户昵称", "用户简介", "关注数", "粉丝数", "性别", "是否认证", "认证原因", "用户主页", "地址")
      .toDF("title", "total", "male", "female", "verified", "unverified")
      //.show(50, false)

    // 隐式转换
    implicit val mySQLConfig: MySQLConfig = SparkToMySQL.mySQLConfig
    SparkToMySQL.dataFrameToMysql(dataFrame, "comment_user")

    spark.stop()
  }
}
