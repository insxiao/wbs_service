package controllers

import models.User
import play.api.mvc._
import services.{AuthenticationService, UserService}
import utils.extractors.BasicAuthorization

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

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

class AuthenticateFilter(implicit val executionContext: ExecutionContext, val authenticationService: AuthenticationService)
  extends ActionRefiner[AuthRequest, UserRequest] {
  override protected def refine[A](request: AuthRequest[A]): Future[Either[Result, UserRequest[A]]] =
    authenticationService.authenticate(request.username, request.password) transform {
      case Success(Some(user)) => Success(Right(new UserRequest[A](user, request)))
      case _ => Success(Left(Results.Unauthorized))
    }
}

trait AuthorizationFunction {
  this: AbstractController =>
  implicit def executionContext: ExecutionContext

  implicit def userService: UserService

  implicit def authenticationService: AuthenticationService

  /**
    * 提取用户名与密码
    * @return AuthorizationFilter
    */
  def authorizationFilter = new AuthorizationFilter

  /**
    * 验证用户名密码
    * @return AuthenticateFilter
    */
  def authenticateCredential = new AuthenticateFilter
}