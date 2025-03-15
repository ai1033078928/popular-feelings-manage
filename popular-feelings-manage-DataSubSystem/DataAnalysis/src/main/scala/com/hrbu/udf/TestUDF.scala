package com.hrbu.udf

import com.hrbu.UserInfo
import com.hrbu.udf.UserInfoUDF.transformationOfGender
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}

object TestUDF {
  val USERINFO_DATA_PATH = "D:\\software\\python\\jupyterFile\\data\\项目数据\\微博个人信息.csv"

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[2]",
    )
    // 创建配置对象
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("TransfromData")
    // 创建SparkSession
    val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    // 导入隐式转换
    import spark.implicits._

    // 加载数据
    val userinfoDF: DataFrame = spark.sparkContext.textFile(USERINFO_DATA_PATH).map(
      line => {
        val attr: Array[String] = line.split("\t")
        UserInfo( attr(0), attr(1).trim, attr(2).trim, attr(3).toInt, attr(4).toInt, attr(5).trim, attr(6).toBoolean, attr(7).trim, attr(8).trim, attr(9).trim )
      }
    ).toDF()

    // 注册UDF
    spark.udf.register("tGender", transformationOfGender _)
    userinfoDF.createOrReplaceTempView("userinfo")
    val frame: DataFrame = spark.sql("select tGender(gender) from userinfo")
    frame.show()



    // 关闭资源
    spark.close()
  }
}
