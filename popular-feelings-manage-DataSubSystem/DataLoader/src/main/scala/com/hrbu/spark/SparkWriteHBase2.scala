package com.hrbu.spark


import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}
//import org.apache.spark.sql.datasources.hbase.HBaseTableCatalog
//import org.apache.spark.sql.execution.datasources.hbase
/*
case class Comment( messageId: String, title: String, text: String, uid: String )

object SparkWriteHBase2 {

  def main0(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("SparkHBaseRDD")
      .master("local")
      .getOrCreate()
    val sc = spark.sparkContext

    def catalog = s"""{
                       |"table":{"namespace":"std", "name":"table1"},
                       |"rowkey":"key",
                       |"columns":{
                         |"rowkey":{"cf":"rowkey", "col":"key", "type":"string"},
                         |"weibotitle":{"cf":"info", "col":"title", "type":"string"},
                         |"commenttext":{"cf":"info", "col":"text", "type":"string"},
                         |"userId":{"cf":"info", "col":"uid", "type":"string"},
                       |}
                     |}""".stripMargin


    val rdd: RDD[String] = sc.makeRDD(
      Array(
        "$中国天眼捕捉罕见快速射电暴三连闪", "4511717843352002", "南仁东！大国重器！", "2032139271",
        "$中国天眼捕捉罕见快速射电暴三连闪", "4511717843352002", "致敬南仁东", "3939254772"
      )
    )

    import spark.implicits._

    val df: DataFrame = rdd.map(
      item => {
        val attr: Array[String] = item.split(",")
        Comment(attr(2), attr(1), attr(3), attr(4))
      }
    ).toDF()


    df.write.options(Map(HBaseTableCatalog.tableCatalog -> catalog, HBaseTableCatalog.newTable -> "2"))
      .format("org.apache.spark.sql.execution.datasources.hbase")
      .save()

    sc.stop()
  }
  */

  /*
  def catalog = s"""{
                     |"table":{"namespace":"default", "name":"table1"},
                     |"rowkey":"key",
                     |"columns":{
                     |"col0":{"cf":"rowkey", "col":"key", "type":"string"},
                     |"col1":{"cf":"cf1", "col":"col1", "type":"boolean"},
                     |"col2":{"cf":"cf2", "col":"col2", "type":"double"},
                     |"col3":{"cf":"cf3", "col":"col3", "type":"float"},
                     |"col4":{"cf":"cf4", "col":"col4", "type":"int"},
                     |"col5":{"cf":"cf5", "col":"col5", "type":"bigint"},
                     |"col6":{"cf":"cf6", "col":"col6", "type":"smallint"},
                     |"col7":{"cf":"cf7", "col":"col7", "type":"string"},
                     |"col8":{"cf":"cf8", "col":"col8", "type":"tinyint"}
                     |}
                     |}""".stripMargin
   */
/*
}
*/
