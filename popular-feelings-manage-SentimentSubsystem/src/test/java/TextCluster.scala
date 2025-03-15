import java.util

import com.hankcs.hanlp.mining.cluster.ClusterAnalyzer
import com.hrbu.config.MyConfig
import com.hrbu.utils.{HBaseUtils, HdfsToSpark}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession


object TextCluster {
  // 数据路径
  val WEIBO_DATA_PATH = "D:\\software\\python\\jupyterFile\\data\\项目数据\\微博.csv"

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[2]",
      "table.name" -> "wordcloud",
      "HT.text_cluster" -> "text_cluster"
    )
    // 创建配置对象
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("TransfromData")
    // 创建SparkSession
    val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    // 使用hanlp进行文本聚类
    val analyzer : ClusterAnalyzer[String] = new ClusterAnalyzer[String]
    // 1.准备微博数据 (注: HdfsToSpark是定义的工具类) 本地(spark.sparkContext.textFile(WEIBO_DATA_PATH))
    HdfsToSpark.getSequenceFileFromFolder(spark.sparkContext, MyConfig.WEIBO_DATA_PATH)
      .map(
        line => {
          val strs: Array[String] = line.split("\\\t+") //匹配多个\t   //split("\\s+") 按空格,制表符，等进行拆分
          //analyzer.addDocument(strs(1), transform(strs(10)))
          //( strs(1), transform(strs(10)) )  //messageId, text
        }
      )
      //.toDF("messageId", "text")

    /*
    //2.求TF-IDF https://blog.csdn.net/weixin_34242331/article/details/93024526?utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EOPENSEARCH%7Edefault-1.control&dist_request_id=&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EOPENSEARCH%7Edefault-1.control
    //2.1 求TF
    val hashingTF = new HashingTF()
      .setInputCol("sentence_words").setOutputCol("rawFeatures").setNumFeatures(500) //2000需要提供一个numFeatures，这个值越大其效果也越好，但是相应的计算时间也越长

    val featurizedData = hashingTF.transform(weiboDF)

    //2.2 求IDF
    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    val idfModel = idf.fit(featurizedData)
    val rescaledData = idfModel.transform(featurizedData).cache()

    //3. 建立模型
    val kmeans = new KMeans().setK(6).setSeed(1L)
    val model = kmeans.fit(rescaledData)
    // Evaluate clustering by computing Within Set Sum of Squared Errors.
    println("calculating wssse ...")
    val WSSSE = model.computeCost(rescaledData)
    println(s"Within Set Sum of Squared Errors = $WSSSE")
     */


    val list: util.List[util.Set[String]] = analyzer.repeatedBisection(10)

    // 创建工具类对象
    val baseUtils: HBaseUtils = HBaseUtils.getInstance()
    if (!baseUtils.tableExists(config("HT.text_cluster"))) {
      // 表不存在则创建表
      baseUtils.createTable(config("HT.text_cluster"), 1, "info")
    }
    // 存入hbase
    for ( i <- 0 until list.size() ) {
      list.get(i).forEach(
        message_id => {
          baseUtils.insertRecord(config("HT.text_cluster"), i.toString, "info", message_id, message_id)
        }
      )
    }

    // 打印结果
    spark.stop()
  }


  /**
  * String 分词
  * @param sentense
  * @return
  */

  /*def transform(sentense:String) : String = {
    import collection.JavaConverters._
    val terms: util.List[Term] = StandardTokenizer.segment(sentense)
    CoreStopWordDictionary.apply(terms)
    terms.asScala.map(x => x.word.replaceAll(" ","")).toString()
  }*/

}
