package com.hrbu

import java.util

import com.hrbu.utils.{HBaseUtils, UniqueOrderGenerate}
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter
import org.apache.hadoop.hbase.{Cell, CellUtil, CompareOperator}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * @title 有状态数据统计
 * 有状态转化操作  (UpdateStateByKey)  类似UDAF的Buffer???
 * 对之前的数据也有更新
 * 保存到文件(数据大  防止宕机???)
 */
object DataStreamingWordCloud {

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "kafka.topic" -> "project_2",
      "HT.word_index" -> "bigdata:word_index",
      "HT.wordcloud" -> "bigdata:wordcloud"
    )
    //创建到Kafka的连接
    val kafkaPara = Map(
      "bootstrap.servers" -> "hadoop1:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "data",
      "auto.offset.reset" -> "earliest",   //latest
      //"enable.auto.commit" -> (false: java.lang.Boolean)  // FIXME: 待修改(应手动维护偏移量) http://www.mamicode.com/info-detail-2266926.html
    )

    // 创建配置对象
    val sparkConf: SparkConf = new SparkConf().setAppName("DataStreaming").setMaster(config("spark.cores"))

    // 创建spark StreamingContext
    val ssc = new StreamingContext(sparkConf, Seconds(5))

    // 保存数据状态 需要设置检查点路径
    //ssc.sparkContext.setCheckpointDir("checkpoint")  // hdfs路径
    ssc.checkpoint("checkpoint")  // FIXME: 存入hdfs小文件问题[https://blog.csdn.net/rlnLo2pNEfx9c/article/details/80553558  https://www.jianshu.com/p/372105903e75]


    val kafkaStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](Array(config("kafka.topic")), kafkaPara)
    )

    // 创建工具类对象
    val baseUtils: HBaseUtils = HBaseUtils.getInstance()
    if (!baseUtils.tableExists(config("HT.word_index")) || !baseUtils.tableExists(config("HT.wordcloud"))) {
      // FIXME: 此处建表是否应该预分区,充分发挥HBase集群的作用
      // 表不存在则创建表
      baseUtils.createTable(config("HT.word_index"), 1, "info")
      baseUtils.createTable(config("HT.wordcloud"), 1, "info")
    }

    val uniqueOrderGenerate = new UniqueOrderGenerate(6, 0)
    /**
     * 数据的有状态保存
     */
    // 取到消息队列里的值
    val valueDStream: DStream[String] = kafkaStream.map(t => t.value())
//    valueDStream.print()

    // 对读入的DStream进行分析
    valueDStream
        .map(
          line => {
            val attr: Array[String] = line.split("\t")
            (attr(0).trim, Ansj_Fenci.participles(attr(2).trim))
          }
        )
        .flatMap { // flatMap算子(一对多)
          case (title, line) => {
            val strings: Array[String] = line.split(" ")
            strings.map(
              (title, _, 1)
            )
          }
        }
        .map(x => (x._1 + "\t" + x._2, x._3))     // 先把前两项拼接到一块 作为key
        .reduceByKey(_ + _)                       // 按key聚合
        /**
         * updateStateByKey有状态计算方法
         * 1.第一个参数 相同key的value的集合
         * 2.第二个参数 相同key的缓冲区数据 可能为空[初始时]
         * 3.这里计算结果需要保存到检查点的位置中,所以要设置检查点路径
         */
        .updateStateByKey[Long](
          ( seq: Seq[Int], buffer: Option[Long] ) => {
            // 将checkpoint数据和新计算的合并
            val newBufferValue: Long = buffer.getOrElse(0L) + seq.sum
            Option(newBufferValue)
          }
        )
        .foreachRDD(
          _.foreachPartition(
            _.foreach(
              // FIXME: 此处代码待优化(存入hbase
              //  1.要查询表是否存在[可以放前面]
              //  2.要一条一条存数据[能不能先存map,随后再存hbase]
              //  3.查询的rowkey是不是可以先放map集合里 避免每次都查)
              // 考虑怎样用二级表存储
              row => {
                val strings: Array[String] = row._1.split("\t")

                if (strings.length == 2) {
                  // (单值过滤)过滤出列值对应的行
                  val filter = new SingleColumnValueFilter(Bytes.toBytes("info"), Bytes.toBytes("title"), CompareOperator.EQUAL, Bytes.toBytes(strings(0)))
                  val cells: util.List[Cell] = baseUtils.scanByFilter(config("HT.word_index"), filter)
                  // 判断标题对应的rowkey是否存在
                  if (cells.size() == 0) {
                    val rowkey = uniqueOrderGenerate.nextId()
                    baseUtils.insertRecord(config("HT.word_index"), rowkey.toString, "info", "title", strings(0))
                    // HBase的列是在建立在列族基础之上的，列可以动态添加； HBase的列在理论上是可以无限添加的
                    baseUtils.insertRecord(config("HT.wordcloud"), rowkey.toString, "info", strings(1), row._2.toString)
                  } else {
                    // 若已存在则更新值 1.获取rowkey 2.插入值
                    val rowkey: String = Bytes.toString(CellUtil.cloneRow(cells.get(0)))
                    baseUtils.insertRecord(config("HT.wordcloud"), rowkey.toString, "info", strings(1), row._2.toString)
                  }

                }

              }
            )
          )
        )
        //.print()

    // 将转换结构后的数据进行聚合处理
    /*val stateDStream: DStream[(String, Int)] = mapDStream.updateStateByKey {
      case (seq, buffer) => {
        val sum: Int = buffer.getOrElse(0) + seq.sum
        Option(sum)
      }
    }*/

    // 启动采集器
    ssc.start()
    // 等待采集器停止
    ssc.awaitTermination()
  }

}
