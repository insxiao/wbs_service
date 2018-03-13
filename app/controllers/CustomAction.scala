package controllers

import models.User
import play.api.mvc._
import services.UserService
import utils.extractors.BasicAuthorization

import scala.concurrent.{ExecutionContext, Future}

class AuthRequest[A](val username: String, val password: String, request: Request[A])
  extends WrappedRequest[A](request)

class UserRequest[A](val user: User, request: Request[A]) extends WrappedRequest[A](request)

class AuthorizationFilter(implicit val executionContext: ExecutionContext) extends ActionRefiner[Request, AuthRequest] {
  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthRequest[A]]] = Future {
    request.headers.get("Authorization") match {
      case Some(BasicAuthorization(username, password)) => Right(new AuthRequest[A](username, password, request))
      case _ => Left(Results.Unauthorized)
    }
  }
}

class UserFilter(implicit val executionContext: ExecutionContext, val userService: UserService) extends ActionRefiner[AuthRequest, UserRequest] {
  override protected def refine[A](request: AuthRequest[A]): Future[Either[Result, UserRequest[A]]] = Future {
    userService.find(request.username).collect {
      case Some(user) if user.password == request.password => Right(new UserRequest[A](user, request))
      case _ => Left(Results.NotFound)
    }.fallbackTo(Future.successful(Left(Results.NotFound)))
  }.flatten
}

trait AuthorizationFunction {
  this: AbstractController =>
  implicit def executionContext: ExecutionContext
  implicit def userService: UserService
  def authorizationFilter = new AuthorizationFilter
  def userFilter = new UserFilter
}