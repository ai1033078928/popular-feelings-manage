package com.hrbu.udf

object UserInfoUDF {

  /**
   * 修改性别格式 (数字化)
   * @param string
   * @return
   */
  def transformationOfGender(string: String): Int ={
    if("m".equalsIgnoreCase(string)){
      // 若为男 返回0
      0
    } else if("f".equalsIgnoreCase(string)) {
      // 若为女 返回0
      1
    } else {
      -1
    }
  }


  def transformationOfLocation(location: String): Int = {
    val map = Map(
      "北京" -> 11,
      "天津" -> 12,
      "河北" -> 13,
      "山西" -> 14,
      "内蒙古" -> 15,
      "辽宁" -> 21,
      "吉林" -> 22,
      "黑龙江" -> 23,
      "上海" -> 31,
      "江苏" -> 32,
      "浙江" -> 33,
      "安徽" -> 34,
      "福建" -> 35,
      "江西" -> 36,
      "山东" -> 37,
      "河南" -> 41,
      "湖北" -> 42,
      "湖南" -> 43,
      "广东" -> 44,
      "广西" -> 45,
      "海南" -> 46,
      "重庆" -> 50,
      "四川" -> 51,
      "贵州" -> 52,
      "云南" -> 53,
      "西藏" -> 54,
      "陕西" -> 61,
      "甘肃" -> 62,
      "青海" -> 63,
      "宁夏" -> 64,
      "新疆" -> 65,
      "台湾" -> 71,
      "香港" -> 81,
      "澳门" -> 82,
      "海外" -> 99,
      "其他" -> 100
    )
    map.get(location).getOrElse(100)
  }

//  def main(args: Array[String]): Unit = {
//    println(transformationOfLocation("河南"))
//  }
}
