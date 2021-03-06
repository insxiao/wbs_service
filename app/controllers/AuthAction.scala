package controllers

import models.{Token, User}
import play.api.Logger
import play.api.mvc._
import services.{AuthenticationService, UserService}
import util.extractors
import util.extractors.{Base64, BasicAuthorization}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class AuthRequest[A](val username: String, val password: String, request: Request[A])
  extends WrappedRequest[A](request)

case class UserRequest[A](user: User, request: Request[A]) extends WrappedRequest[A](request)

case class TokenRequest[A](token: Token, request: Request[A]) extends WrappedRequest[A](request)

case class TokenOptionRequest[A](token: Option[Token], request: Request[A]) extends WrappedRequest[A](request)

class TokenAuthenticate(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[Request, TokenRequest] {
  private val logger = Logger(classOf[TokenAuthenticate])

  import util.extractors

  override protected def refine[A](request: Request[A]): Future[Either[Result, TokenRequest[A]]] = Future {
    val remoteAddress = request.remoteAddress
    request.cookies.get("token")
      .map(_.value)
      .map {
        case Base64(credentials) => credentials
      }
      .map { v =>
        logger.debug(s"$remoteAddress\ttoken => $v")
        v
      }
      .flatMap {
        case extractors.Token(id, name) =>
          logger.debug(s"$remoteAddress token data $id $name")
          Some(TokenRequest(Token(id, name), request))
        case _ =>
          logger.info(s"$remoteAddress authenticate failed")
          None
      }.toRight(Results.Unauthorized)
  }
}

class TokenOptionTransformer(implicit val executionContext: ExecutionContext)
  extends ActionTransformer[Request, TokenOptionRequest] {
  override protected def transform[A](request: Request[A]): Future[TokenOptionRequest[A]] = Future {
    request.cookies.get("token")
      .map(_.value)
      .map {
        case Base64(credentials) => credentials
      }
      .flatMap {
        case extractors.Token(id, name) =>
          Some(TokenOptionRequest(Some(Token(id, name)), request))
        case _ => None
      }.getOrElse(TokenOptionRequest(None, request))
  }
}

/**
  * 将通过Token验证的用户信息提取出来
  *
  * @param executionContext [[scala.concurrent.ExecutionContext]] execution context
  * @param userService      [[services.UserService]] user service
  */
class TokenTransformer(implicit val executionContext: ExecutionContext, private val userService: UserService) extends ActionTransformer[TokenRequest, UserRequest] {
  override protected def transform[A](request: TokenRequest[A]): Future[UserRequest[A]] =
    userService.find(request.token.id).map { case Some(user) => UserRequest(user, request) }
}


class AuthorizationFilter(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[Request, AuthRequest] {
  private val logger = Logger(classOf[AuthorizationFilter])

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthRequest[A]]] = Future {
    request.headers.get("Authorization") match {
      case Some(BasicAuthorization(username, password)) =>
        logger.debug(s"authorization with $username, $password")
        Right(new AuthRequest[A](username, password, request))
      case other =>
        logger.debug(s"authorization filter failed with $other")
        Left(Results.Unauthorized)
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


/**
  * ActionFunctions
  * functions for login
  *
  * authorizationFilter Decode Authorization header and create AuthRequest
  * authenticateCredential Authenticate username and password. And create UserRequest from AuthRequest
  *
  * functions for token
  *
  * tokenAuthenticate create TokenRequest from Cookie or Session
  * tokenTransformer  transformer TokenRequest to UserRequest
  */
trait AuthorizationFunction {
  this: AbstractController =>
  implicit def executionContext: ExecutionContext

  implicit def userService: UserService = implicitly[UserService]

  implicit def authenticationService: AuthenticationService = implicitly[AuthenticationService]

  /**
    * authenticate with cookie token
    *
    * @return
    */
  def tokenAuthenticate = new TokenAuthenticate

  /**
    * if logged in token is some token
    * else token is none
    * @return
    */
  def tokenOptionTransformer = new TokenOptionTransformer

  def tokenTransformer = new TokenTransformer

  /** @todo need usage */
  private def filterUser: ActionFunction[Request, UserRequest] = authorizationFilter andThen authenticateCredential

  /**
    * 提取用户名与密码
    *
    * @return AuthorizationFilter
    */
  def authorizationFilter = new AuthorizationFilter

  /**
    * 验证用户名密码
    *
    * @return AuthenticateFilter
    */
  def authenticateCredential = new AuthenticateFilter
}