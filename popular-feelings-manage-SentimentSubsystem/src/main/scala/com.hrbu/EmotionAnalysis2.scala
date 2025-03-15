package com.hrbu

import java.io.File

import com.hankcs.hanlp.classification.classifiers.NaiveBayesClassifier
import com.hankcs.hanlp.classification.models.{AbstractModel, NaiveBayesModel}
import com.hankcs.hanlp.corpus.io.IOUtil
import com.hrbu.config.MyConfig
import com.hrbu.utility.TestUtility
import com.hrbu.utils.{HdfsToSpark, SparkToMySQL}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * @DO 评论情感正负判断
 * 数据集:https://github.com/SophonPlus/ChineseNlpCorpus
 */
object EmotionAnalysis2 {
  // 模型路径
  private val MODEL_PATH: String = "D:\\Program Files\\JetBrains\\ideaProjects\\popular-feelings-manage\\data\\myModel.ser"
  // 数据路径
  //val COMMENT_DATA_PATH = "D:\\software\\python\\jupyterFile\\data\\项目数据\\微博评论.csv"
  //val WEIBO_DATA_PATH = "D:\\software\\python\\jupyterFile\\data\\项目数据\\微博.csv"

  def main(args: Array[String]): Unit = {

    val config = Map(
      "spark.cores" -> "local[*]",
    )
    // 创建配置对象
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("EmotionAnalysis2")
    // 创建SparkSession
    val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    // 隐式转换
    import spark.implicits._
    // 准备评论数据 (注: HdfsToSpark是定义的工具类) 本地spark.sparkContext.textFile(COMMENT_DATA_PATH)
    val emotionRDD: RDD[(String, String, String, String)] = HdfsToSpark.getSequenceFileFromFolder(spark.sparkContext, MyConfig.COMMENT_DATA_PATH)
      .map(
        line => {
          val strs: Array[String] = line.split("\t")
          val classifier = trainOrGetMyModel()    // 获取分类器
          (strs(1), classifier.classify(strs(2)), strs(2), strs(3)) // 预测情感
        }
      )

    val emotionDF: DataFrame = emotionRDD
      .map(row => (row._1, row._2))
      .groupByKey()
      .map {
        case (id, arr) => {
          var z = 0
          for (elem <- arr) {
            if (elem.equals("正面")) z += 1
          }
          (id, z / arr.size.toDouble)
        }
      }
      .toDF("messageId", "positive")

    // 准备微博数据 (注: HdfsToSpark是定义的工具类) 本地(spark.sparkContext.textFile(WEIBO_DATA_PATH))
    val weiboDF: DataFrame = HdfsToSpark.getSequenceFileFromFolder(spark.sparkContext, MyConfig.WEIBO_DATA_PATH)
      .map(
        line => {
          // 这里又发现一个问题 "484.0".toDouble.toInt
          val strs: Array[String] = line.split("\\\t+") //匹配多个\t   //split("\\s+") 按空格,制表符，等进行拆分
          val classifier = trainOrGetMyModel()    // 获取分类器
          (strs(0), strs(1), strs(6), strs(8), strs(9), strs(10), strs(11).toDouble, strs(12).toDouble, strs(13).toDouble, classifier.classify(strs(10)))
        }
      )
      .toDF("title", "messageId", "createTime", "userName", "messageUrl", "text", "repostsCount", "commentsCount", "attitudesCount", "weiboEmotion")

    val frame: DataFrame = weiboDF.join(emotionDF, "messageId")
    //frame.show()

    // 准备评论用户数据 (注: HdfsToSpark是定义的工具类) 本地(spark.sparkContext.textFile())
    val userInfoDF: DataFrame = HdfsToSpark.getSequenceFileFromFolder(spark.sparkContext, MyConfig.USERINFO_DATA_PATH)
      .map {
        line => {
          val strs: Array[String] = line.split("\\\t+") //匹配多个\t   //split("\\s+") 按空格,制表符，等进行拆分
          (strs(0).trim, strs(1).trim, strs(2).trim, strs(3).toDouble.toInt, strs(4).toDouble.toInt, strs(5).trim, strs(6).toBoolean, strs(7).trim, strs(8).trim, strs(9).trim)
        }
      }
      .toDF("uid", "name", "uDesc", "followCount", "followersCount", "gender", "verified", "verifiedReason", "uUrl", "location")

    // 保存到数据库
    // 隐式参数 (注: SparkToMySQL是定义的工具类)
    implicit val mySQLConfig = SparkToMySQL.mySQLConfig
    SparkToMySQL.dataFrameToMysql(frame, "weibo")
    SparkToMySQL.dataFrameToMysql(emotionRDD.toDF("messageId", "commentEmotion", "commentBody", "uid"), "comment")
    SparkToMySQL.dataFrameToMysql(userInfoDF, "userinfo")

    // 打印结果
    spark.stop()
  }


  /**
   * 从磁盘读入模型[若读入失败则创建模型]
   * 函数中调用了trainNaiveBayesClassifier
   */
  def trainOrGetMyModel():NaiveBayesClassifier = {
    // 若模型不存在则创建模型
    if (!new File(MODEL_PATH).exists()){
      trainNaiveBayesClassifier()
    } else {
      // 从文件读取模型
      val model:NaiveBayesModel = IOUtil.readObjectFrom(MODEL_PATH).asInstanceOf[NaiveBayesModel]
      new NaiveBayesClassifier(model)
    }
  }

  /**
   * 根据语料集[中文情感挖掘语料-ChnSentiCorp 谭松波]创建判断感情正负向模型
   * @return 分类器
   */
  def trainNaiveBayesClassifier():NaiveBayesClassifier = {
    // 中文情感挖掘语料-ChnSentiCorp 谭松波
    val CORPUS_FOLDER: String = TestUtility.ensureTestData("ChnSentiCorp情感分析酒店评论", "http://file.hankcs.com/corpus/ChnSentiCorp.zip")
    val corpusFolder = new File(CORPUS_FOLDER)
    // 查找文件目录不存在 则退出
    if (!corpusFolder.exists() || !corpusFolder.isDirectory()) {
      System.err.println("没有文本分类语料，请阅读IClassifier.train(java.lang.String)中定义的语料格式、准备语料")
      System.exit(1)
      null
    } else {
      val classifier:NaiveBayesClassifier = new NaiveBayesClassifier()         // 创建分类器，更高级的功能请参考IClassifier的接口定义
      classifier.train(CORPUS_FOLDER, "UTF-8")                 // 训练后的模型支持持久化，下次就不必训练了
      // 持久化模型
      val model: NaiveBayesModel = classifier.getModel.asInstanceOf[NaiveBayesModel]
      IOUtil.saveObjectTo(model, MODEL_PATH)
      classifier
    }
  }

}
