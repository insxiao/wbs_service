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

trait UserRepository {
  def findBy(id: Long): Option[User]

  def findBy(id: Long, password: Array[Char]): Option[User]

  def save(user: User): Try[User]

  def findMyFollowers(user: User): List[User]
}

trait PostRepository {
  def findByUserId(userId: Long): List[MicroPost]

  def post(mp: MicroPost): Try[MicroPost]
}






