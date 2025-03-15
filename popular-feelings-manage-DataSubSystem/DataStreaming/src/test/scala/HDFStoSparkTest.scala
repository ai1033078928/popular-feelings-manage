import org.apache.hadoop.io.{BytesWritable, LongWritable, Text}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.Test

class HDFStoSparkTest {

  val config = Map(
    "spark.cores" -> "local[2]",
  )

  @Test
  def getDataFromFile(): Unit ={
    // 创建配置对象
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("getDataFromFile")
    val sc: SparkContext = SparkContext.getOrCreate(sparkConf)

    /*sc.hadoopFile("hdfs://hadoop1:9000/flume/bigdata/weibo/2020-08-07.1596730169053.csv", classOf[TextInputFormat], classOf[LongWritable], classOf[Text])
      .map(
        pair => new String(pair._2.getBytes, 0, pair._2.getLength, "utf-8")
      )
      .foreach(x => println(x + "\t"))*/

    /**
     * 每一个SequenceFile都包含一个“头”（header)。Header包含了以下几部分。
     * SEQ!org.apache.hadoop.io.LongWritable"org.apache.hadoop.io.BytesWritable
     *
     * 1.SEQ三个字母的byte数组
     * 2.Version number的byte，目前为数字3的byte
     * 3.Key和Value的类名
     * 4.压缩相关的信息
     * 5.其他用户定义的元数据
     * 6.同步标记，sync marker
     */
    /*sc.textFile("hdfs://hadoop1:9000/flume/bigdata/weibo/2020-08-07.1596730169053.csv")
        .foreach(println(_))*/

    val rdd: RDD[(Long, String)] = sc.sequenceFile("hdfs://hadoop1:9000/flume/bigdata/weibo/2020-08-07.1596730169053.csv", classOf[LongWritable], classOf[BytesWritable])
          .map { case (k, v) => (k.get(), new String(v.copyBytes())) }

    rdd.foreach(println)

    sc.stop()
  }

  @Test
  def getDataFromFolder(): Unit ={
    // 创建配置对象
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("getDataFromFile")
    val sc: SparkContext = SparkContext.getOrCreate(sparkConf)

    sc.hadoopConfiguration.set("mapreduce.input.fileinputformat.input.dir.recursive", "true")

    sc.sequenceFile("hdfs://hadoop1:9000/flume/bigdata/userinfo/*.csv", classOf[LongWritable], classOf[BytesWritable])
      .map { case (k, v) => new String(v.copyBytes()) }
      .take(20)
      .foreach(println)


    sc.stop()
  }
}
