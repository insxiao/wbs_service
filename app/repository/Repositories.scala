package repository

import domain.{MicroPost, User}

import scala.util.Try

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


