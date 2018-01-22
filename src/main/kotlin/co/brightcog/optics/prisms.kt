package co.brightcog.optics
import arrow.*

@prisms sealed class Json
object JNull : Json()
@lenses data class JStr(val v: String): Json()
@lenses data class JNum(val v: Double): Json()
@lenses data class JObj(val v: Map<String, Json>): Json()




fun main(s: Array<String>) {

    val jStr: Json = JStr("foo")
    val jNum: Json = JNum(1.0)

    val o1 = jsonJStr().getOrModify(jStr)
    val o2 = jsonJStr().getOption(jNum)
    val o3 = jsonJNum().getOption(jNum)
    println("$o1, $o2, $o3")
}