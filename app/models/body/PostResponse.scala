package models.body

import models.MicroBlog
import play.api.libs.json.{Format, Json, Writes}

case class PostResponse(posts: Seq[MicroBlog], next: Next)

object PostResponse {
  implicit val PostResponseWrites: Writes[PostResponse] = Json.writes[PostResponse]
}
