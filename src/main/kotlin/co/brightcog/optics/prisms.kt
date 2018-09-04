package co.brightcog.optics
import arrow.optics.optics

@optics sealed class Json {
  companion object {}
  object JNull : Json()
  data class JStr(val v: String): Json()
  data class JNum(val v: Double): Json()
  data class JObj(val v: Map<String, Json>): Json()
}





fun main(s: Array<String>) {

//    val jStr: Json = Json.JStr("foo")
//    val jNum: Json = Json.JNum(1.0)
//    val o1 = Json.jStr.getOrModify(jStr)
//    val o2 = Json.jStr.getOption(jNum)
//    val o3 = Json.jStr.getOption(jNum)
//    println("$o1, $o2, $o3")
}
