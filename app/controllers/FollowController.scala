package controllers

import java.time.LocalDateTime

import javax.inject.{Inject, Singleton}
import models.User
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import services.{FollowService, UserService}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class FollowController @Inject()(val cc: ControllerComponents)
                                (implicit val executionContext: ExecutionContext,
                                 override val userService: UserService,
                                 val followService: FollowService)
  extends AbstractController(cc) with AuthorizationFunction {

  private lazy val logger = Logger(classOf[FollowService])

  def listFollowers = (Action andThen tokenAuthenticate) async {
    implicit request: Request[_] =>

      followService.listFollowers(request.id).transform {
        case Success(Seq()) => Success(NoContent)
        case Success(follows) => Success(Ok(Json.toJson(follows)))
        case Failure(e: Throwable) =>
          logger.warn(s"${request.remoteAddress}    ${e.getMessage}")
          Success(InternalServerError)
      }
  }

  def get(userId: Long): Action[AnyContent] = (Action andThen tokenAuthenticate) async {
    implicit request =>
      val followerId = request.token.id
      followService.get(userId, followerId) transform {
        case Success(Some(user)) => Success(Ok(Json.toJson(user)))
        case Success(None) => Success(NoContent)
        case Failure(e: Throwable) =>
          logger.warn(s"${request.remoteAddress}  get followed user ${e.getMessage}")
          Success(InternalServerError)
      }
  }

  def listFollows: Action[AnyContent] = (Action andThen tokenAuthenticate) async {
    implicit request =>
      followService.listFollows(request.token.id) transform {
        case Success(Seq()) => Success(NoContent)
        case Success(followUsers) => Success(Ok(Json.toJson(followUsers)))
        case Failure(e: Throwable) =>
          logger.warn(s"${request.remoteAddress}  list follows  ${e.getMessage}")
          Success(InternalServerError)
      }
  }

  def follow: Action[JsValue] = (Action(parse.json) andThen tokenAuthenticate) async {
    implicit request =>
      val userIdOpt = (request.body \ "userId").asOpt[Long]
      logger.warn(s"${request.remoteAddress}  follow  params: ${userIdOpt}")
      if (userIdOpt.nonEmpty) {
        followService.follow(request.token.id, userIdOpt.get) transform {
          case Success(_) => Success(Ok)
          case Failure(e: Throwable) =>
            logger.warn(s"${request.remoteAddress}  follow  ${e.getMessage}")
            Success(InternalServerError)
        }
      } else {
        Future.successful(BadRequest(Json.obj("message" -> "用户ID错误")))
      }
  }

  def unfollow(): Action[JsValue] = (Action(parse.json) andThen tokenAuthenticate) async {
    implicit request =>
      val followerId = request.token.id
      val userIdOpt = (request.body \ "userId").asOpt[Long]
      logger.warn(s"${request.remoteAddress}  follow  params: $userIdOpt")
      if (userIdOpt.isEmpty) {
        Future.successful(BadRequest)
      } else {
        followService.unfollow(followerId, userIdOpt.get) transform {
          case Success(n) => Success(Ok)
          case Failure(e: Throwable) =>
            logger.warn(s"${request.remoteAddress}  unfollow  ${e.getMessage}")
            Success(BadRequest)
        }
      }
  }
}
