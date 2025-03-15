package cn.hrbu.test.wordcount

import TransformData.participles
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Word2Vec_Test {
  val COMMENT_DATA_PATH = "D:\\software\\python\\jupyterFile\\data\\项目数据\\微博评论.csv"

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[2]",
    )
    // 创建配置对象
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("TransfromData")
    // 创建 SparkContext
    val sc: SparkContext = new SparkContext(sparkConf)

    val commentRDD: RDD[String] = sc.textFile(COMMENT_DATA_PATH)

    val testRDD: RDD[List[String]] = commentRDD.map(
      line => {
        val attr: Array[String] = line.split("\t")
        participles(attr(2).trim).split(" ").toList
      }
    )

    val word2Vec = new Word2Vec()
    word2Vec.setSeed(42)
    val word2VecModel: Word2VecModel = word2Vec.fit(testRDD)

    word2VecModel.findSynonyms("大学", 5).foreach(println(_))


    sc.stop()
  }

}
