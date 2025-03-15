package com.hrbu.utils

import com.hrbu.config.MyConfig
import org.apache.spark.sql.{DataFrame, SaveMode}

object SparkToMySQL {

  // 隐式参数
  //implicit val mySQLConfig = MySQLConfig(config("mysql.url"), config("mysql.user"), config("mysql.password"))
  val mySQLConfig = MySQLConfig(MyConfig.MySQLConfig("mysql.url"), MyConfig.MySQLConfig("mysql.user"), MyConfig.MySQLConfig("mysql.password"))

  def dataFrameToMysql(df: DataFrame, dbtable: String)(implicit mySQLConfig: MySQLConfig): Unit = {
    df.write.mode(SaveMode.Overwrite)
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
/**
 * mysql连接配置
 */
case class MySQLConfig(url: String, user: String, password: String)
