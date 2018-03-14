package models

import java.time.LocalDateTime

case class Comment(id: Option[Long], blogId: Long, content: String, stars: Int, userId: Long, timestamp: LocalDateTime)

object Comment {
  import play.api.libs.json.{Json, Format}

  implicit val commentFormat: Format[Comment] = Json.format[Comment]
}