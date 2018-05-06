package controllers

import java.time.LocalDateTime

import javax.inject.{Inject, Singleton}
import models.User
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
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
}
