package cn.hrbu.test.sometestcode

import java.util

import scala.io.Source

object Test2 {
  val STOP_WORDS = "popular-feelings-manage-DataSubSystem/DataAnalysis/library/stop_words.txt"

  def main(args: Array[String]): Unit = {

    val arrayList = new util.ArrayList[String]()

    for (line <- Source.fromFile(STOP_WORDS).getLines){
      arrayList.add(line)
    }

    println(arrayList)
  }
}
