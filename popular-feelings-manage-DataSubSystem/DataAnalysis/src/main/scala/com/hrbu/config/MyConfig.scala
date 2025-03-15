package com.hrbu.config

object MyConfig {
  val WEIBO_DATA_PATH = "hdfs://hadoop1:9000/flume/bigdata/weibo/*.csv"
  val COMMENT_DATA_PATH = "hdfs://hadoop1:9000/flume/bigdata/comment/*.csv"
  val USERINFO_DATA_PATH = "hdfs://hadoop1:9000/flume/bigdata/userinfo/*.csv"

  // 本地mysql
/*  val MySQLConfig = Map(
    "mysql.url" -> "jdbc:mysql://192.168.1.6:3306/bigdata?serverTimezone=UTC&useUnicode=true",
    "mysql.user" -> "banana",
    "mysql.password" -> "banana"
  )*/

  // CentOS7 mysql (这样也可以)
  val MySQLConfig = Map(
    "mysql.url" -> "jdbc:mysql://8.131.51.250:3306/bigdata?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=UTF-8&useSSL=false",
    "mysql.user" -> "root",
    "mysql.password" -> "0D9be677df95"
  )
}
