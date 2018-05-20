package services

import javax.inject.{Inject, Singleton}

import models.{Repository, User}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
trait AuthenticationService {
  def repository: Repository
  implicit def executionContext: ExecutionContext

  def authenticate(username: String, password: String): Future[Option[User]] = {
    repository.Users.find(username).collect {
      case Some(user) if user.password.contains(password) => Some(user)
      case _ => None
    }
  }
}
