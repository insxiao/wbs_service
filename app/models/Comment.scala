package models

case class Comment(id: Option[Long], content: String, stars: Int, userId: Long)

object Comment {
  import play.api.libs.json.{Json, Format}

  implicit val commentFormat: Format[Comment] = Json.format[Comment]
}