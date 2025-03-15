package com.hrbu

import java.io.File
import java.util

import com.hankcs.hanlp.classification.classifiers.{IClassifier, NaiveBayesClassifier}
import com.hankcs.hanlp.classification.models.NaiveBayesModel
import com.hankcs.hanlp.corpus.io.IOUtil
import com.hankcs.hanlp.mining.cluster.ClusterAnalyzer
import com.hrbu.config.MyConfig
import com.hrbu.utility.TestUtility
import com.hrbu.utils.{ HBaseUtils, HdfsToSpark, SparkToMySQL}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}


object TextCluster {

  /**
   * 搜狗文本分类语料库5个类目，每个类目下1000篇文章，共计5000篇文章
   */
  val CORPUS_FOLDER = TestUtility.ensureTestData("搜狗文本分类语料库迷你版", "http://file.hankcs.com/corpus/sogou-text-classification-corpus-mini.zip");
  /**
   * 模型保存路径
   */
  val MODEL_PATH = "D:\\Program Files\\JetBrains\\ideaProjects\\popular-feelings-manage\\data/classification-model.ser";

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[2]"
    )
    // 创建配置对象
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("text_cluster")
    // 创建SparkSession
    val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    // 使用hanlp进行文本聚类
    val model: NaiveBayesModel = trainOrLoadModel()

    import spark.implicits._
    // 1.准备微博数据 (注: HdfsToSpark是定义的工具类) 本地(spark.sparkContext.textFile(WEIBO_DATA_PATH))
    val frame: DataFrame = HdfsToSpark.getSequenceFileFromFolder(spark.sparkContext, MyConfig.WEIBO_DATA_PATH)
      .map(
        line => {
          val strs: Array[String] = line.split("\\\t+") //匹配多个\t   //split("\\s+") 按空格,制表符，等进行拆分
          val classifier: IClassifier = new NaiveBayesClassifier(model)
          (classifier.classify(strs(10)), strs(1))
          //( strs(1), transform(strs(10)) )  //messageId, text
        }
      )
      .toDF("class", "messageId")


    // 保存到数据库
    // 隐式参数 (注: SparkToMySQL是定义的工具类)
    implicit val mySQLConfig = SparkToMySQL.mySQLConfig
    SparkToMySQL.dataFrameToMysql(frame, "")

    // 打印结果
    spark.stop()
  }


  private def trainOrLoadModel() : NaiveBayesModel = {
    var model: NaiveBayesModel = IOUtil.readObjectFrom(MODEL_PATH).asInstanceOf[NaiveBayesModel]
    if (model != null) return model

    val corpusFolder : File = new File(CORPUS_FOLDER)
    if (!corpusFolder.exists() || !corpusFolder.isDirectory())
    {
      System.err.println("没有文本分类语料，请阅读IClassifier.train(java.lang.String)中定义的语料格式与语料下载：" +
        "https://github.com/hankcs/HanLP/wiki/%E6%96%87%E6%9C%AC%E5%88%86%E7%B1%BB%E4%B8%8E%E6%83%85%E6%84%9F%E5%88%86%E6%9E%90")
      System.exit(1)
    }

    val classifier : IClassifier = new NaiveBayesClassifier() // 创建分类器，更高级的功能请参考IClassifier的接口定义
    classifier.train(CORPUS_FOLDER)                     // 训练后的模型支持持久化，下次就不必训练了
    model = classifier.getModel().asInstanceOf[NaiveBayesModel]
    IOUtil.saveObjectTo(model, MODEL_PATH);
    return model;
  }
}
