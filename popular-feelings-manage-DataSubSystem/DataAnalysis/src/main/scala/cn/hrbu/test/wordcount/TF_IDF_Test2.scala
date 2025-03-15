package cn.hrbu.test.wordcount

import breeze.linalg.CSCMatrix
import TransformData.participles
//import org.apache.spark.mllib.clustering.{LDA, LDAModel}
import org.apache.spark.mllib.feature.{HashingTF, IDF, IDFModel}
import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.{SparseVector => SV}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object TF_IDF_Test2 {
  val COMMENT_DATA_PATH = "D:\\software\\python\\jupyterFile\\data\\项目数据\\微博评论.csv"
  val GET_MESSAGE = "$闺蜜是拿来利用的吗"

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[2]",
    )
    // 创建配置对象
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("TransfromData")
    // 创建 SparkContext
    val sc: SparkContext = new SparkContext(sparkConf)

    val commentRDD: RDD[String] = sc.textFile(COMMENT_DATA_PATH)


    /**
     * 用软件包(提取特征?)计算 每个词的 TF-IDF 值
     * 1.训练词频矩阵
     */
    val testRDD: RDD[String] = commentRDD.map(
      line => {
        val attr: Array[String] = line.split("\t")
        // 分词
        participles(attr(2).trim)
      }
    )

    val documents: RDD[Seq[String]] = testRDD
      .map(_.split(" ").toSeq)
    // 维度大小 默认为1 << 20=1048576  这里使用2^18=262144(应该够用了吧,更小点?)
    val hashingTF = new HashingTF(1 << 18)
    // 把每个词序列映射到mllib的vector对象
    val tf: RDD[linalg.Vector] = hashingTF.transform(documents) // 底层使用MurmurHash3_x86_32算法 实现特征散列
    //    for (tf_ <- tf) { println(s"$tf_") }
    // 调用cache把数据持久在内存
    //tf.cache()

    // 查看第一个向量
    val vector: SV = tf.first().asInstanceOf[SV]
    println(vector.size) // 稀疏向量维度
    println(vector.values.size) // 非零项
    println(vector.values.take(10).toSeq) // 下标
    println(vector.indices.take(10).toSeq) // 词频值

    /**
     * 用软件包(提取特征?)计算 每个词的 TF-IDF 值
     * 2.计算 TF-IDF 矩阵
     */
    // 利用词频向量作为输入对语料集中的每个单词计算逆文本频率
    val iDFModel: IDFModel = new IDF()
      .fit(tf)
    // 将词频向量转换为TF-IDF向量
    val tfIDF: RDD[linalg.Vector] = iDFModel.transform(tf)

    tfIDF.take(10).foreach(println)

    // val tf_zipIDF: RDD[(Long, linalg.Vector)] = tfIDF.zipWithIndex().map(row => (row._2, row._1))
    //    println(s"tfIDF size: ${tfIDF.count()}")
    //for (tfIDF_ <- tfIDF) { println(s"$tfIDF_") }

    sc.stop()
  }
}
