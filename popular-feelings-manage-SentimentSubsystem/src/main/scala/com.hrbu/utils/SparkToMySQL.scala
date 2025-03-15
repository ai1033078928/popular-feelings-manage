package com.hrbu.utils

import com.hrbu.config.MyConfig
import org.apache.spark.sql.{DataFrame, SaveMode}

object SparkToMySQL {

  // 隐式参数
  //implicit val mySQLConfig = MySQLConfig(config("mysql.url"), config("mysql.user"), config("mysql.password"))
  val mySQLConfig = MySQLConfig(MyConfig.MySQLConfig("mysql.url"), MyConfig.MySQLConfig("mysql.user"), MyConfig.MySQLConfig("mysql.password"))

  /**
   * 保存DataFrame到数据库
   * 数据写入mysql的时候是按照分区来写的，也就是说每个分区都创建了一个mysql连接，于是我在写入mysql之前对DataFrame先进行分区，根据mysql连接池数量设定合理的分区
   * @param df
   * @param dbtable
   * @param mySQLConfig
   */
  def dataFrameToMysql(df: DataFrame, dbtable: String)(implicit mySQLConfig: MySQLConfig): Unit = {
    df.write.mode(SaveMode.Overwrite)
      .format("jdbc")
      .option("url", mySQLConfig.url)
      .option("dbtable", dbtable)
      .option("user", mySQLConfig.user)
      .option("password", mySQLConfig.password)
      .option("driver", "com.mysql.cj.jdbc.Driver")
      .option("isolationLevel", "NONE")     // 事务隔离级别，DataFrame写入不需要开启事务，为NONE
      .option("batchsize", "10000")     // DataFrame writer批次写入MySQL 的条数，也为提升性能参数
      .option("truncate", "true")       // overwrite模式时可用，表时在覆盖原始数据时不会删除表结构而是复用
      .save()
  }

}
/**
 * mysql连接配置
 */
case class MySQLConfig(url: String, user: String, password: String)
