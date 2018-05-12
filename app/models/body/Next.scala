package models.body

import play.api.libs.json._

import scala.language.postfixOps

/**
  * @param url    next url
  * @param params only primative type
  */
case class Next(url: String, params: Map[String, Any])

object Next {

  implicit val nextWrites: Writes[Next] = new Writes[Next] {
    override def writes(o: Next): JsValue = JsObject(Seq(
      "params" -> JsObject(o.params.mapValues {
        case n: Short => JsNumber(n)
        case n: Int => JsNumber(n)
        case n: Long => JsNumber(n)
        case n: Float => JsNumber(n)
        case n: Double => JsNumber(n)
        case str: String => JsString(str)
        case char: Char => JsString(char.toString)case n: Short => JsNumber(n)
        case Some(n: Int) => JsNumber(n)
        case Some(n: Long) => JsNumber(n)
        case Some(n: Float) => JsNumber(n)
        case Some(n: Double) => JsNumber(n)
        case Some(str: String) => JsString(str)
        case Some(char: Char) => JsString(char.toString)
        case _ => JsNull
      }),
      "url" -> JsString(o.url)))
  }
}