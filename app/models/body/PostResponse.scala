package models.body

import models.MicroBlog
import play.api.libs.json.{Format, Json}

case class PostResponse(posts: Seq[MicroBlog], next: String)


object PostResponse {
  implicit val PostResponseFormat: Format[PostResponse] = Json.format[PostResponse]
}
