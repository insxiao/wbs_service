package models
import java.time.LocalDate

import play.api.libs.json.Format

case class MicroBlog(id: Option[Long], content: String, postDate: LocalDate, ownerId: Long)


object MicroBlog {
  import play.api.libs.json.Json

  implicit val microBlog: Format[MicroBlog] = Json.format[MicroBlog]
}