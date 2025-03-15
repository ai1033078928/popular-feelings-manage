package com.hrbu

import java.util

import org.ansj.recognition.impl.StopRecognition
import org.ansj.splitWord.analysis.NlpAnalysis
import org.ansj.util.MyStaticValue

import scala.io.Source

object Ansj_Fenci {
  val STOP_WORDS = "D:\\Program Files\\JetBrains\\ideaProjects\\popular-feelings-manage\\popular-feelings-manage-DataSubSystem\\DataAnalysis\\library\\stop_words.txt"

  /**
   * 设置停用词 过滤一些无用的字和词
   * @param list
   * @return
   */
  def stopRecognitionFilter(list: util.List[String]): StopRecognition = {
    // 这个方法是从别的博客摘抄而来 https://blog.csdn.net/zh519080/article/details/81224621
    //数字和量词合并
    MyStaticValue.isQuantifierRecognition = true
    val stopRecognition = new StopRecognition
    //识别评论中的介词（p）、叹词（e）、连词（c）、代词（r）、助词（u）、字符串（x）、拟声词（o）
    //stopRecognition.insertStopNatures("p", "e", "c", "r", "u", "x", "o")
    stopRecognition.insertStopNatures("w") //剔除标点符号
    //剔除以中文数字开头的，以一个字或者两个字为删除单位，超过三个的都不删除
    stopRecognition.insertStopRegexes("^一.{0,2}", "^二.{0,2}", "^三.{0,2}", "^四.{0,2}", "^五.{0,2}",
      "^六.{0,2}", "^七.{0,2}", "^八.{0,2}", "^九.{0,2}", "^十.{0,2}")
    stopRecognition.insertStopNatures("null") //剔除空
    //stopRecognition.insertStopRegexes(".{0,1}") //剔除只有一个汉字的
    stopRecognition.insertStopRegexes("^[a-zA-Z]{1,}") //把分词只为英文字母的剔除掉
    stopRecognition.insertStopWords(list) //添加停用词
    stopRecognition.insertStopRegexes("^[0-9]+") //把分词只为数字的剔除
    stopRecognition.insertStopRegexes("[^a-zA-Z0-9\u4e00-\\u9fa5]+") //把不是汉字、英文、数字的剔除
    stopRecognition
  }

  /**
   * 对DataFrame某一列进行分词统计
   * @param
   */
  def participles(str: String): String ={

    // scala List转为java List   import  scala.collection.JavaConverters._
    val arrayList = new util.ArrayList[String]()
    // scala读取文件  [停用词表有待更新]
    for (line <- Source.fromFile(STOP_WORDS).getLines){
      arrayList.add(line)
    }

    //分词准备
    val recognition: StopRecognition = stopRecognitionFilter(arrayList)

    // 自然语言分词,具有未登录词发现功能。建议在自然语言理解中用。搜索中不要用
    val result: String = NlpAnalysis.parse(str)  //Nlp分词 速度稍慢 40w字/s?
      .recognition(recognition)
      .toStringWithOutNature(" ")

    // 精准分词
    //val result: String = ToAnalysis.parse(str) // (str, forest)
    //  .recognition(recognition)
    //  .toStringWithOutNature(" ")  // 分词默认会打出词性，此语句用于不打出词性，并且分好的词用“ ”隔开

    result
  }

}
