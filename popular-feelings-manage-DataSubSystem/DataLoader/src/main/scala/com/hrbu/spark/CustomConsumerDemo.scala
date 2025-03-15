package com.hrbu.spark

import java.time.Duration
import java.util.{Arrays, Properties}

import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}


/**
 * 消费方式
 * consumer 采用 pull（拉） 模式从 broker 中读取数据。
 * push（推）模式很难适应消费速率不同的消费者，因为消息发送速率是由 broker 决定的
 */
/**
 * pull 模式不足之处是，如果 kafka 没有数据，消费者可能会陷入循环中， 一直返回空数
 * 据。 针对这一点， Kafka 的消费者在消费数据时会传入一个时长参数 timeout，如果当前没有
 * 数据可供消费， consumer 会等待一段时间之后再返回，这段时长即为 timeout
 */
object CustomConsumerDemo {
  //自动提交 offset
  def consume(): Unit ={
    val properties = new Properties()
    properties.put("bootstrap.servers", "hadoop1:9092")
    properties.put("group.id", "test")
    properties.put("enable.auto.commit", "true") //是否开启自动提交 offset 功能
    properties.put("auto.commit.interval.ms", "1000") //自动提交 offset 的时间间隔
    properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")  // 反序列化
    properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

    val consumer = new KafkaConsumer[String, String](properties)
    consumer.subscribe(Arrays.asList("calllog"))
    while (true) {
      val records: ConsumerRecords[String, String] = consumer.poll(Duration.ofMillis(100))
      records.forEach(record => println(f"offset= ${record.offset()}\tkey= ${record.key()}\tvalue= ${record.value()}"))
    }
    consumer.close()
  }
  def main(args: Array[String]): Unit = {
    CustomConsumerDemo.consume()
  }
}
