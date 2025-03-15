package com.hrbu

import com.hrbu.config.MyConfig
import com.hrbu.utils.{Ansj_Fenci, HdfsToSpark, SparkToMySQL}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.SparkConf

/*运行(23:03:57-23:04)*/
object WordCloudData {
  //val COMMENT_DATA_PATH = "D:\\software\\python\\jupyterFile\\data\\项目数据\\微博评论.csv"
  //val GET_MESSAGE = "$中国天眼捕捉罕见快速射电暴三连闪"

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[2]",
      "table.name" -> "wordcloud"
    )
    // 创建配置对象
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("TransfromData")
    // 创建SparkSession
    val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()
    // 创建 SparkContext
    //val sc: SparkContext = spark.sparkContext

    // 读取数据 本地spark.sparkContext.textFile(COMMENT_DATA_PATH) (注: HdfsToSpark是定义的工具类)
    val commentRDD: RDD[String] = HdfsToSpark.getSequenceFileFromFolder(spark.sparkContext, MyConfig.COMMENT_DATA_PATH)

    // 对评论进行(过滤后)分词
    val toAnalysisRDD: RDD[(String, String)] = commentRDD.map(
      line => {
        val attr: Array[String] = line.split("\t")
        (attr(0).trim, Ansj_Fenci.participles(attr(2).trim))
      }
    )

    import spark.implicits._
    // 分词计数
    val wordColudDF: DataFrame = toAnalysisRDD
      .flatMap { // flatMap算子(一对多)
        case (title, line) => {
          val strings: Array[String] = line.split(" ")
          strings.map(
            (title, _, 1)
          )
        }
      }
      .map(x => (x._1 + "\t" + x._2, x._3))     // 先把前两项拼接到一块 作为key
      .reduceByKey(_ + _)                       // 按key聚合
      .map(x => {                               // 把key切分
        val str: Array[String] = x._1.split("\t")
        if (str.length > 1){
          (str(0), str(1), x._2)
        } else {
          (str(0), "", x._2)
        }
      })
      .filter(x => x._3 > 1 && x._2 != null)     // 过滤(空值和词数==1)的行 .filter(x => x._3 > 1 && x._2 != null)
      .toDF("title", "word", "number")     // 转为DataFrame

    wordColudDF.show()

    // 隐式参数 (注: SparkToMySQL是定义的工具类)
    implicit val mySQLConfig = SparkToMySQL.mySQLConfig
    SparkToMySQL.dataFrameToMysql(wordColudDF, config("table.name"))

    spark.stop()

  }

}
