package cn.hrbu.test.sometestcode

import java.util

import org.ansj.recognition.impl.StopRecognition
import org.ansj.splitWord.analysis.ToAnalysis
import org.ansj.util.MyStaticValue
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

object Test {
  def main(args: Array[String]): Unit = {

    val config = Map(
      "spark.cores" -> "local[2]",
    )
    // 创建配置对象
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("TransfromData")
    // 创建SparkSession
    val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()
    val sc: SparkContext = spark.sparkContext

    // 准备数据
    val dataFrame: DataFrame = spark.createDataFrame(Seq(
      (0, "进行文本分析前，对文本中句子进行分词我们处理的第一步"),
      (1, "大家都是Spark的机器学习库分为基于RDD和基于DataFrame的库"),
      (2, "由于基于RDD的库在Spark2.0以后都处于维护状态，"),
      (3, "我们这里讲的分词就是基于Spark的Dataframe的"),
      (4, "主要是讲解两个类Tokenizer和RegexTokenizer的使用。"),
      (5, "首先准备数据"),
      (6, "Tokenizer负责读取文档或者句子，将其分解为单词。声明一个变量")
    )).toDF("id", "info")

    // 导入隐式转换

    val arrayList = new util.ArrayList[String]()
    arrayList.add("美国")
    arrayList.add("去年")
    arrayList.add("分词")
    //分词准备
    val recognition: StopRecognition = stopRecognitionFilter(arrayList)

    // 加载用户自定义词典 val DIC_PATH = ""
    // DicLibrary.put("dic", DIC_PATH)
    // val forest: Forest = Library.makeForest(classOf[Nothing].getResourceAsStream("popular-feelings-manage-DataSubSystem/DataAnalysis/library/2000000-dict.txt")) //加载字典文件
    //val forest: Forest = Library.makeForest("popular-feelings-manage-DataSubSystem/DataAnalysis/library/2000000-dict.txt") //加载字典文件

    val str = "进行文本分析前，对文本中句子进行分词是我们处理的第一步 美国去年 10元 my name"

    val result: String = ToAnalysis.parse(str)// (str, forest)
      .recognition(recognition)
      .toStringWithOutNature(" ")  // 分词默认会打出词性，此语句用于不打出词性，并且分好的词用“,”隔开

    println(result)

    spark.stop()

  }

  // 这个方法是从别的博客摘抄而来 https://blog.csdn.net/zh519080/article/details/81224621
  def stopRecognitionFilter(arrayList: util.ArrayList[String]): StopRecognition = {
    MyStaticValue.isQuantifierRecognition = true //数字和量词合并
    val stopRecognition = new StopRecognition
    //识别评论中的介词（p）、叹词（e）、连词（c）、代词（r）、助词（u）、字符串（x）、拟声词（o）
    stopRecognition.insertStopNatures("p", "e", "c", "r", "u", "x", "o")
    stopRecognition.insertStopNatures("w") //剔除标点符号
    //剔除以中文数字开头的，以一个字或者两个字为删除单位，超过三个的都不删除
    stopRecognition.insertStopRegexes("^一.{0,2}", "^二.{0,2}", "^三.{0,2}", "^四.{0,2}", "^五.{0,2}",
      "^六.{0,2}", "^七.{0,2}", "^八.{0,2}", "^九.{0,2}", "^十.{0,2}")
    stopRecognition.insertStopNatures("null") //剔除空
    stopRecognition.insertStopRegexes(".{0,1}") //剔除只有一个汉字的
    stopRecognition.insertStopRegexes("^[a-zA-Z]{1,}") //把分词只为英文字母的剔除掉
    stopRecognition.insertStopWords(arrayList) //添加停用词
    stopRecognition.insertStopRegexes("^[0-9]+") //把分词只为数字的剔除
    stopRecognition.insertStopRegexes("[^a-zA-Z0-9\u4e00-\\u9fa5]+") //把不是汉字、英文、数字的剔除
    stopRecognition
  }

}
