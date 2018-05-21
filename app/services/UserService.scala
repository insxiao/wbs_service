package services

import java.time.LocalDateTime

import javax.inject.{Inject, Singleton}
import models.{Repository, User}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.JsValue

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject()(override val repository: Repository)
                           (implicit ec: ExecutionContext)
  extends AuthenticationService {

  val logger = Logger(classOf[UserService])

  override implicit def executionContext: ExecutionContext = ec

  def create(user: User): Future[User] = repository.Users.create(user)

  def list(): Future[Seq[User]] = repository.Users.list()

  def delete(id: Long): Future[Int] = repository.Users.delete(id)

  def delete(username: String): Future[Int] = repository.Users.delete(username)

  def find(id: Long): Future[Option[User]] = repository.Users.find(id)

  def find(username: String): Future[Option[User]] = repository.Users.find(username)

  def exists(id: Long): Future[Boolean] = repository.Users.exists(id)

  def exists(username: String): Future[Boolean] = repository.Users.exists(username)

  def changePassword(id: Long, password: String): Future[Int] = repository.Users.changePassword(id, password)

  def search(q: String, offset: Int = 0, size: Int = 10): Future[Seq[User]] = repository.Users.search(q, offset, size)

  def update(user: User): Future[List[Int]] = repository.Users.update(user)
}


