package cn.hrbu.test

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object DataStreaming2 {
  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[3]",
      "kafka.topic" -> "project_3",
      "HT.word_index" -> "bigdata:word_index",
      "HT.wordcloud" -> "bigdata:wordcloud"
    )

    // 创建配置对象
    val sparkConf: SparkConf = new SparkConf().setAppName("DataStreaming").setMaster(config("spark.cores"))

    // 创建spark StreamingContext
    val ssc = new StreamingContext(sparkConf, Seconds(5))

    // 保存数据状态 需要设置检查点路径
    //ssc.sparkContext.setCheckpointDir("checkpoint")  // hdfs路径
    ssc.checkpoint("checkpoint")

    //创建到Kafka的连接
    val kafkaPara = Map(
      "bootstrap.servers" -> "hadoop1:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "data",
      "auto.offset.reset" -> "earliest"   //latest
    )

    val kafkaStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](Array(config("kafka.topic")), kafkaPara)
    )

    // 取到消息队列里的值
    val userInfoDStream: DStream[String] = kafkaStream.map(t => t.value())


    // 启动采集器
    ssc.start()
    // 等待采集器停止
    ssc.awaitTermination()
  }
}
