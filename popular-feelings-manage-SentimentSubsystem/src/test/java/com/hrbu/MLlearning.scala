package com.hrbu

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.{functions => SqlFunc}

object MLlearning {

  def main(args: Array[String]): Unit = {
    // 读取的路径和文件
    val file = "D:\\software\\python\\jupyterFile\\text\\tmall_order_report.csv"

    // 创建配置对象
    val sparkConf: SparkConf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("MLlearning")

    // 创建SparkSession
    val spark: SparkSession = SparkSession.builder()
      .config(sparkConf)
      .getOrCreate()

    val df: DataFrame = spark
      .read
      .option("delimiter", ",")
      .option("header", "true")    //将第一行设置为字段名称
      .option("quote", "'")
      .option("nullValue", "\\N")
      .option("inferSchema", "true")
      .csv(file)

    df.groupBy("收货地址")
        .agg(SqlFunc.sum("总金额"))
        .as("总金额")
        .show(100)

//    df.show(200)

    // 打印结果
    spark.stop()
  }

}
