package cn.hrbu.test.wordcount

import java.util

import org.ansj.recognition.impl.StopRecognition
import org.ansj.splitWord.analysis.NlpAnalysis
import org.ansj.util.MyStaticValue
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.io.Source


/**
 * 获取数据(文件)  并对数据进行处理
 */

/**
 * 微博数据
 */
case class Weibo(title: String, messageId: String, createTime: String, userName: String,
                 messageUrl: String, text: String, repostsCount: Int, commentsCount: Int, attitudesCount: Int)
/**
 * 评价数据
 */
case class Comment(rowkey: String, title: String, messageId: String, text: String, uid: String)

/**
 * 评价用户个人信息
 */
case class UserInfo(uid: String, name: String, uDesc: String, followCount: Int, followersCount: Int, gender: String,
                    verified: Boolean, verifiedReason: String, uUrl: String, location: String)

object TransformData {

  // 定义数据存储路径
  private val COMMENT_DATA_PATH = "D:\\software\\python\\jupyterFile\\data\\项目数据\\微博评论.csv"
  private val USERINFO_DATA_PATH = "D:\\software\\python\\jupyterFile\\data\\项目数据\\微博个人信息.csv"
  private val GET_MESSAGE = "$闺蜜是拿来利用的吗"
  //val STOP_WORDS = "popular-feelings-manage-DataSubSystem/DataAnalysis/library/stop_words.txt"
  private val STOP_WORDS = "D:\\Program Files\\JetBrains\\ideaProjects\\popular-feelings-manage\\popular-feelings-manage-DataSubSystem\\DataAnalysis\\library\\stop_words.txt"

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
    val commentDF: DataFrame = spark.sparkContext.textFile(COMMENT_DATA_PATH).map(
      line => {
        val attr: Array[String] = line.split("\t")
        // Weibo( str(0), str(1), str(6), str(8), str(9), str(10), str(11).toInt,str(12).toInt, str(13).toInt )
        // UserInfo(attr(0).toInt, attr(1).trim, attr(2).trim, attr(3).toInt, attr(4).toInt, attr(5).trim, attr(6).toBoolean, attr(7).trim, attr(8).trim, attr(9).trim)
        Comment( (attr(1)+attr(3)).trim, attr(0).trim, attr(1).trim, participles(attr(2).trim), attr(3).trim )
      }
    ).toDF()

    val userinfoDF: DataFrame = spark.sparkContext.textFile(USERINFO_DATA_PATH).map(
      line => {
        val attr: Array[String] = line.split("\t")
        UserInfo( attr(0), attr(1).trim, attr(2).trim, attr(3).toInt, attr(4).toInt, attr(5).trim, attr(6).toBoolean, attr(7).trim, attr(8).trim, attr(9).trim )
      }
    ).toDF()

    // 加入filter后, 会优化join, 先执行过滤
    val joinDF: DataFrame = commentDF.join(userinfoDF, Seq("uid"), "inner") //commentDF("uid") === userinfoDF("uid")
      .filter(commentDF("title") === GET_MESSAGE)  // 这里返回值是DataSet(有自动类型转换???)
      //.filter(userinfoDF("followersCount") > 100000)

    joinDF.show(false)
    //joinDF.select("text").show(false)

    /*
    // 创建临时视图
    commentDF.createOrReplaceTempView("comment")
    // 按标题查找
    val selectDF: DataFrame = spark.sql("select * from comment where title = '$闺蜜是拿来利用的吗'")
    // 遍历打印
    selectDF.foreach(println(_))
    */

    // 关闭资源
    spark.close()

  }

  /**
   * 设置停用词 过滤一些无用的字和词
   * @param list
   * @return
   */
  def stopRecognitionFilter(list: util.List[String]): StopRecognition = {
    // 这个方法是从别的博客摘抄而来 https://blog.csdn.net/zh519080/article/details/81224621
    //数字和量词合并
    MyStaticValue.isQuantifierRecognition = true
    val stopRecognition = new StopRecognition
    //识别评论中的介词（p）、叹词（e）、连词（c）、代词（r）、助词（u）、字符串（x）、拟声词（o）
    //stopRecognition.insertStopNatures("p", "e", "c", "r", "u", "x", "o")
    stopRecognition.insertStopNatures("w") //剔除标点符号
    //剔除以中文数字开头的，以一个字或者两个字为删除单位，超过三个的都不删除
    stopRecognition.insertStopRegexes("^一.{0,2}", "^二.{0,2}", "^三.{0,2}", "^四.{0,2}", "^五.{0,2}",
      "^六.{0,2}", "^七.{0,2}", "^八.{0,2}", "^九.{0,2}", "^十.{0,2}")
    stopRecognition.insertStopNatures("null") //剔除空
    //stopRecognition.insertStopRegexes(".{0,1}") //剔除只有一个汉字的
    stopRecognition.insertStopRegexes("^[a-zA-Z]{1,}") //把分词只为英文字母的剔除掉
    stopRecognition.insertStopWords(list) //添加停用词
    stopRecognition.insertStopRegexes("^[0-9]+") //把分词只为数字的剔除
    stopRecognition.insertStopRegexes("[^a-zA-Z0-9\u4e00-\\u9fa5]+") //把不是汉字、英文、数字的剔除
    stopRecognition
  }

  /**
   * 对DataFrame某一列进行分词统计
   * @param
   */
  def participles(str: String): String ={

    // scala List转为java List   import  scala.collection.JavaConverters._
    val arrayList = new util.ArrayList[String]()
    // scala读取文件  [停用词表有待更新]
    for (line <- Source.fromFile(STOP_WORDS).getLines){
      arrayList.add(line)
    }

    //分词准备
    val recognition: StopRecognition = stopRecognitionFilter(arrayList)

    // 自然语言分词,具有未登录词发现功能。建议在自然语言理解中用。搜索中不要用
    val result: String = NlpAnalysis.parse(str)  //Nlp分词 速度稍慢 40w字/s?
      .recognition(recognition)
      .toStringWithOutNature(" ")   // 分词默认会打出词性，此语句用于不打出词性，并且分好的词用“ ”隔开

    // 精准分词
    //val result: String = ToAnalysis.parse(str) // (str, forest)
    //  .recognition(recognition)
    //  .toStringWithOutNature(" ")  // 分词默认会打出词性，此语句用于不打出词性，并且分好的词用“ ”隔开

    result
  }

  def getWeibo(spark: SparkSession): Unit ={
    import spark.implicits._
    // 加载数据
    val weiboDF: DataFrame = spark.sparkContext.textFile("").map(
      line => {
        val str: Array[String] = line.split("\t")
        Weibo( str(0), str(1), str(6), str(8), str(9), str(10), str(11).toInt,str(12).toInt, str(13).toInt )
      }
    ).toDF()
  }
}
