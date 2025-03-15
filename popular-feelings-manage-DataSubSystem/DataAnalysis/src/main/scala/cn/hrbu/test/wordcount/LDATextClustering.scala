package cn.hrbu.test.wordcount

import org.apache.spark.ml.clustering.{LDA, LDAModel}
import org.apache.spark.ml.linalg
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

object LDATextClustering {

  def main(args: Array[String]): Unit = {

    val config = Map(
      "spark.cores" -> "local[2]",
    )
    // 创建配置对象
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("TransfromData")
    // 创建 SparkSession
    val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()
    // 创建 SparkContext
    val sc: SparkContext = spark.sparkContext



    // 词典  好像不是这个样子????先伪装一下
    val vocabArray = Map(
      (43, "真的"),
      (39, "利用"),
      (25, "觉得"),
    )

    // ML基本类型  1.向量Vector(稀疏 稠密)  2.标量LabledPoint  3.矩阵Matrix(类似python的ndarray) 列优先  | 皮尔逊相关系数和斯皮尔曼相关系数
    // 构造算法参数 思考该怎样构造微博文本向量???
    import spark.implicits._
    val documents: Dataset[(Long, linalg.Vector)] = sc.makeRDD(
      Seq(
        (1L, Vectors.sparse(4, Array(0, 3), Array(1, -2.0))),
        (2L, Vectors.dense(4.0, 5.1, 7.3, 6.2))
      )
    ).toDS()

    // 训练模型  思考参数含义及选择???
    val lda: LDA = new LDA()
      .setOptimizer("em")  // 参数估计算法(最大期望算法√)(在线变分贝叶斯算法)
      .setK(20)  // 聚类中心数Ｋ
      .setMaxIter(50)  // 最大迭代次数
      .setDocConcentration(-1)
      .setTopicConcentration(-1)
      .setCheckpointInterval(10)

    val lDAModel: LDAModel = lda.fit(documents)

    // 抽取话题
    // 通过describeTopics方法选取其中权值最高的 前10个词组成词向量来描述话题
    val allTopicsIndices: DataFrame = lDAModel.describeTopics(maxTermsPerTopic = 10)
    println("The topics described by their top-weighted terms:")
    allTopicsIndices.show(false)


    val dataset: Dataset[(Long, linalg.Vector)] = documents
    // Shows the result.
    val transformed = lDAModel.transform(dataset)
    transformed.show(false)

    sc.stop()
    spark.stop()
  }

}
