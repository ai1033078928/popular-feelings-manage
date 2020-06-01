package com.hrbu

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.mutable

/**
 * Spark Streaming整合Kafka
 *
 * @author zhiying.dong@hand-china.com 2019/05/24 16:54
 */
object KafkaDirect{
  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf()
      .setAppName("DirectKafka")
      .setMaster("local[2]")

    val ssc = new StreamingContext(conf, Seconds(2))

    val topicsSet = Array("calllog")
    val kafkaParams: mutable.HashMap[String, String] = mutable.HashMap[String, String]()
    //必须添加以下参数，否则会报错
    kafkaParams.put("bootstrap.servers", "hadoop1:9092")
    kafkaParams.put("group.id", "test")
    kafkaParams.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    kafkaParams.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

    val messages: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topicsSet, kafkaParams)
    )

    // Get the lines, split them into words, count the words and print
    val lines: DStream[String] = messages.map(_.value)
    val words: DStream[String] = lines.flatMap(_.split(" "))
    val wordCounts: DStream[(String, Long)] = words.map(x => (x, 1L)).reduceByKey(_ + _)
    wordCounts.print()

    // Start the computation
    ssc.start()
    ssc.awaitTermination()
  }
}