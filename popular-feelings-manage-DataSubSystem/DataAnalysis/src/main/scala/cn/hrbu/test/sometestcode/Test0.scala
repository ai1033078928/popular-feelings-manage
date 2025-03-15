package cn.hrbu.test.sometestcode

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Test0 {

  def main(args: Array[String]): Unit = {
    val sparkConf: SparkConf = new SparkConf().setAppName("app").setMaster("local")
    val sc = new SparkContext(sparkConf);

    val rdd = sc.makeRDD(
      Seq(
        "hello world !!!",
        "Apache Hadoop 3.2.1 incorporates a number of significant",
        "Node Attributes helps to tag multiple labels on the nodes",
        "Supports HDFS (Hadoop Distributed File System) applications"
      )
    )

    // flatMap + map 的区别
    val value: RDD[String] = rdd.flatMap(_.split(" "))

    val value1: RDD[Array[String]] = rdd.map(_.split(" "))

    sc.stop()
  }

}
