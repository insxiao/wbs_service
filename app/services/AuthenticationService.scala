package services

import javax.inject.{Inject, Singleton}

import models.{User, UserRepository}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
trait AuthenticationService {
  def userRepository: UserRepository
  implicit def executionContext: ExecutionContext

  def authenticate(username: String, password: String): Future[Option[User]] = {
    userRepository.find(username).collect {
      case Some(user) if user.password == password => Some(user)
      case _ => None
    }
  }
}
