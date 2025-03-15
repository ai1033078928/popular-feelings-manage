package com.hrbu.utils

import org.apache.hadoop.io.{BytesWritable, LongWritable}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

object HdfsToSpark {

  /**
   * 传入hdfs目录, 读入该目录下所有文件(SequenceFile类型)
   * @param sc
   */
  def getSequenceFileFromFolder(sc: SparkContext, hdfsPath: String): RDD[String] = {
    // 读入SequenceFile类型文件 需要设置
    sc.hadoopConfiguration.set("mapreduce.input.fileinputformat.input.dir.recursive", "true")

    // path = "hdfs://hadoop1:9000/flume/bigdata/weibo/*.csv"
    val rdd = sc.sequenceFile(hdfsPath, classOf[LongWritable], classOf[BytesWritable])
      .map { case (k, v) => new String(v.copyBytes()) }

    // 返回rdd
    rdd
  }

}
