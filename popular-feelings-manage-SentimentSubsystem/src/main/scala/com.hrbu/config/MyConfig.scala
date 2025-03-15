package com.hrbu.config

object MyConfig {
  val WEIBO_DATA_PATH = "hdfs://hadoop1:9000/flume/bigdata/weibo/*.csv"
  val COMMENT_DATA_PATH = "hdfs://hadoop1:9000/flume/bigdata/comment/*.csv"
  val USERINFO_DATA_PATH = "hdfs://hadoop1:9000/flume/bigdata/userinfo/*.csv"

  val MySQLConfig = Map(
    // rewriteBatchedStatements=true  MySQL服务开启批次写入，此参数是批次写入的一个比较重要参数，可明显提升性能
    "mysql.url" -> "jdbc:mysql://localhost:3306/bigdata?serverTimezone=UTC&useUnicode=true&rewriteBatchedStatements=true",
    "mysql.user" -> "root",
    "mysql.password" -> "123456789"
  )
}
