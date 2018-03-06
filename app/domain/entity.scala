package domain

import java.util.Date

import slick.jdbc.PostgresProfile._
import slick.jdbc.PostgresProfile.api._

import scala.util.Try




case class User(id: Long,
                name: String,
                gender: Gender,
                birthday: Date,
                email: String,
                phone: String)

case class MicroPost(id: Long, content: String, postDate: Date, ownerId: Long)






