package models

import java.time.LocalDateTime

import play.api.libs.json.Format

case class MicroBlog(id: Option[Long], content: String, timestamp: LocalDateTime, userId: Long)


object MicroBlog {

  import play.api.libs.json.Json

  implicit val microBlog: Format[MicroBlog] = Json.format[MicroBlog]
}