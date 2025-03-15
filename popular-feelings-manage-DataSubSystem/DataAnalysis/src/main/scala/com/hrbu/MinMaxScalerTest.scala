package com.hrbu

import com.hrbu.config.MyConfig
import com.hrbu.utils.{HdfsToSpark, SparkToMySQL}
import org.apache.spark.ml.feature.MinMaxScaler
import org.apache.spark.ml.linalg.{DenseVector, Vectors}
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.{DataFrame, SparkSession, functions}

/*运行(23:02-23:03)*/
/**
 * @DO 选出top10话题
 * 把微博的转发 评论 点赞归一化
 */
object MinMaxScalerTest {
  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[2]",
    )

    // 创建SparkSession对象
    val spark: SparkSession = SparkSession.builder().master(config("spark.cores")).appName("MinMaxScalerTest").getOrCreate()

    // 创建数据 (注: HdfsToSpark是定义的工具类) 本地spark.sparkContext.textFile("popular-feelings-manage-DataSubSystem/DataAnalysis/data/微博.csv")
    import spark.implicits._
    val dataFrame: DataFrame = HdfsToSpark.getSequenceFileFromFolder(spark.sparkContext, MyConfig.WEIBO_DATA_PATH)
      .map(
        line => {
          val attr: Array[String] = line.split("\t")
          // 搜索词 微博id 稠密向量(转发 评论 点赞)
          (attr(0), attr(1), Vectors.dense(attr(11).toDouble, attr(12).toDouble, attr(13).toDouble))
        }
      ).toDF("title", "weibo_id", "features")

    // 先打印一下
    //dataFrame.show(false)

    // MinMaxScaler 作用于每一列，即每一维特征。将每一维特征线性地映射到指定的区间，默认是[0, 1]
    val scaler = new MinMaxScaler()
      .setInputCol("features")    // 设置输入
      .setOutputCol("scaledFeatures")   // 设置输出

    // 训练生成MinMaxScalerModel
    val scalerModel = scaler.fit(dataFrame)

    // 缩放每个特征到范围[最小，最大]  每transform一次就加一列
    val scaledData = scalerModel.transform(dataFrame)
    println(s"Features scaled to range: [${scaler.getMin}, ${scaler.getMax}]")
    //scaledData.select("features", "scaledFeatures").show(false)
    //scaledData.show(false)

    /*
    def extractUdf:UserDefinedFunction = org.apache.spark.sql.functions.udf((vectors: SDV) => {
      val array: Array[Double] = vectors.toArray
      array(0)*0.5 + array(1)*0.3 + array(2)*0.2
      })*/
    // 为什么Vectors不行,一直报错???
    def extractUdf:UserDefinedFunction = functions.udf((v: DenseVector) => {
      val array: Array[Double] = v.toArray
      BigDecimal.valueOf(array(0)*0.5 + array(1)*0.3 + array(2)*0.2)
    })

    val dataFrame1: DataFrame = scaledData.withColumn("weight", extractUdf($"scaledFeatures"))

    /**
     * 排序取前n行
     */
    val dataFrame2: DataFrame = dataFrame1.sort(dataFrame1("weight").desc) // sort函数和orderBy用法和结果是一样的
      .select("weibo_id", "weight")
      .limit(20)
      .rdd
      .zipWithIndex()
      .map(
        row => {
          (row._1.getString(0), row._1.getDecimal(1), row._2)
        }
      ).toDF("weibo_id", "weight", "id")
    //dataFrame2.show(100, false)

    /**
     * 保存进Mysql数据库
     */
    // 隐式参数 (注: SparkToMySQL是定义的工具类)
    implicit val mySQLConfig = SparkToMySQL.mySQLConfig
    SparkToMySQL.dataFrameToMysql(dataFrame2, "topn")

    spark.stop()
  }

}
