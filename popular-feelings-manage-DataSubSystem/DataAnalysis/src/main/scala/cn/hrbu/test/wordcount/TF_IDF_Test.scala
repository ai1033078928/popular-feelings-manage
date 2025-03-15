package cn.hrbu.test.wordcount

import TransformData.participles
import breeze.linalg.CSCMatrix
import org.apache.spark.mllib.clustering.{LDA, LDAModel}
import org.apache.spark.mllib.feature.{HashingTF, IDF, IDFModel}
import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.{SparseVector => SV}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer

object TF_IDF_Test {
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

    /*
    // 对评论进行(过滤后)分词
    val toAnalysisRDD: RDD[(String, String, String, String)] = commentRDD.filter(
      line => {
        val attr: Array[String] = line.split("\t")
        attr(0).equals(GET_MESSAGE)
      }
    ).map(
      line => {
        val attr: Array[String] = line.split("\t")
        // if? 这里能加过滤吗
        (attr(0).trim, attr(1).trim, participles(attr(2).trim), attr(3).trim)
      }
    )
    // 缓存 计算快 但消耗资源
    //toAnalysisRDD.cache()
    */

    /*
    // 分词计数 并排序
    val wordCountsRDD: RDD[(String, Int)] = toAnalysisRDD.flatMap {
      case (_, _, words, _) => {
        words.split(" ").map((_, 1))
      }
    }.reduceByKey(_ + _)
      .map(x => (x._2, x._1))
      .sortByKey(false) // 按key倒序
      .map(x => (x._2, x._1))
    // 缓存 计算快 但消耗资源
    //wordCountsRDD.cache()

    // 打印数目 (628 - 517)
    // println("wordCountsRDD=" + wordCountsRDD.count())
    // 打印内容
    //wordCountsRDD.collect().foreach(println(_))
    */

    // 创建词典 1.
    /*
    val comment_terms_dic = new ListBuffer[String]()
    val comment_terms: Array[String] = wordCountsRDD.map(r => r._1).distinct().collect()
    for (term <- comment_terms) {
      comment_terms_dic += term
    }
    println(comment_terms_dic.length)
    println(comment_terms_dic.indexOf("闺蜜"))
    */

    /*
    // 创建词典 2.
    val comment_terms_withZip: collection.Map[String, Long] = wordCountsRDD.map(r => r._1).distinct().zipWithIndex().collectAsMap()
    //println(comment_terms_withZip.get("闺蜜"))

    // 测试数据
    val testRDD: RDD[String] = sc.makeRDD(
      Seq(
        "真的 感觉 不行 人 人 之间 最 信任 ",
        "工作 去 利用 闺蜜 希望 女孩 都 擦亮 眼 人 闺蜜",
        "真的 气 死 看 电视剧 不 投入 太 真情实感"
      )
    )
    // 从评论创建稀疏向量
    val term_matrix: RDD[CSCMatrix[Int]] = testRDD.map(
      row => {
        val strings: Array[String] = row.split(" ")
        create_vector(strings, comment_terms_withZip)
      }
    )
    term_matrix.foreach(println)
    */

    /**
     * 用软件包(提取特征?)计算 每个词的 TF-IDF 值
     */
    val testRDD: RDD[String] = commentRDD.map(
      line => {
        val attr: Array[String] = line.split("\t")
        participles(attr(2).trim)
      }
    )

    val documents: RDD[Seq[String]] = testRDD.map(_.split(" ").toSeq)
    // 维度大小 默认为1 << 20=1048576  这里使用2^18=262144(应该够用了吧,更小点?)
    val hashingTF = new HashingTF(1 << 18)
    // 把每个词序列映射到mllib的vector对象
    val tf: RDD[linalg.Vector] = hashingTF.transform(documents) // 底层使用MurmurHash3_x86_32算法 实现特征散列
    //    for (tf_ <- tf) {
    //      println(s"$tf_")
    //    }
    // 调用cache把数据持久在内存
    tf.cache()

    // 查看第一个向量
    val vector: SV = tf.first().asInstanceOf[SV]
    println(vector.size) // 稀疏向量维度
    println(vector.values.size) // 非零项
    println(vector.values.take(10).toSeq) // 下标
    println(vector.indices.take(10).toSeq) // 词频值

    // 利用词频向量作为输入对语料集中的每个单词计算逆文本频率
    val iDFModel: IDFModel = new IDF().fit(tf)
    // 将词频向量转换为TF-IDF向量
    val tfIDF: RDD[linalg.Vector] = iDFModel.transform(tf)
    val tf_zipIDF: RDD[(Long, linalg.Vector)] = tfIDF.zipWithIndex().map(row => (row._2, row._1))
    //    println(s"tfIDF size: ${tfIDF.count()}")
    //for (tfIDF_ <- tfIDF) {
    //  println(s"$tfIDF_")
    //}

    /**
     * LDA降维
     */
    val lda = new LDA()
      .setOptimizer("em")  // 参数估计算法(最大期望算法√)(在线变分贝叶斯算法)
      .setK(20)  // 聚类中心数Ｋ
      .setMaxIterations(50)  // 最大迭代次数
      .setDocConcentration(-1)
      .setTopicConcentration(-1)
      .setCheckpointInterval(10)
    val lDAModel: LDAModel = lda.run(tf_zipIDF)
    // 抽取话题
    val allTopicsIndies: Array[(Array[Int], Array[Double])] = lDAModel.describeTopics(maxTermsPerTopic = 10)
    allTopicsIndies.map {
      case (terms, termWeights) => {
        terms.zip(termWeights).map{
          case (term, weight) => ((term), weight)
        }
      }
    }

    /**
     * 分析TF-IDF权重
     */
    /*
        val minMaxVals = tfIDF.map { v =>
          val sv = v.asInstanceOf[SV]
          (sv.values.min, sv.values.max)
        }
        val globalMinMax = minMaxVals.reduce {
          case ((min1, max1),
          (min2, max2)) =>
            (math.min(min1, min2), math.max(max1, max2))
        }
        println(globalMinMax)
    */
    /*
    val common = sc.parallelize(Seq(Seq("上去","上来","上述","上面","下列"))) //Seq("一下", "一些", "一切", "一则", "一天") Seq("真的", "气", "死", "看", "电视剧", "不", "投入", "真情实感")
    val tfCommon = hashingTF.transform(common)
    val tfidfCommon = iDFModel.transform(tfCommon)
    val commonVector = tfidfCommon.first.asInstanceOf[SV]
    println("常用词:" + commonVector.values.toSeq)

    val common1 = sc.parallelize(Seq(Seq("真的", "气", "死", "看", "电视剧", "不", "投入", "真情实感")))
    val tfCommon1 = hashingTF.transform(common1)
    val tfidfCommon1 = iDFModel.transform(tfCommon1)
    val commonVector1 = tfidfCommon1.first.asInstanceOf[SV]
    println("不常用词:" + commonVector1.values.toSeq)
    */

    sc.stop()
  }

  /**
   * 从字符串数组创建稀疏矩阵
   *
   * @param comment_terms
   * @param terms_dic
   * @return
   */
  def create_vector(comment_terms: Array[String], terms_dic: collection.Map[String, Long]): CSCMatrix[Int] = {
    // 词在字典中的位置(下标)
    var idx = 0
    // 创建1 * 词典长度的矩阵 (类似列名?)
    val matrix: CSCMatrix[Int] = CSCMatrix.zeros[Int](1, terms_dic.size)
    // 遍历评论分词后的词列表
    comment_terms.foreach(
      term => {
        idx = terms_dic.getOrElse(term, -1L).toInt // 如果找不到 则为-1
        // 若果能找到, 添加到稀疏矩阵
        if (idx != -1) {
          matrix.update(0, idx, 1) // 行 列 值
        }
      }
    )
    matrix
  }

}
