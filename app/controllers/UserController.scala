package controllers

import javax.inject.{Inject, Singleton}

import models.{Repository, Token}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsError, Json, Reads}
import play.api.mvc._
import services.{AuthenticationService, UserService}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class UserController @Inject()(cc: ControllerComponents)
                              (implicit val executionContext: ExecutionContext, override val userService: UserService)
  extends AbstractController(cc) with AuthorizationFunction {

  private val logger = Logger(classOf[UserController])

  import models.User

  def register: Action[User] = Action(validateUserJson) async { request =>
    val user = request.body
    userService.create(user).map(user => Json.toJson(user)).map(Ok(_)).recoverWith {
      case e: Throwable => userService.exists(user.name).transform {
        case Success(true) => Success(Conflict(Json.obj("reason" -> "user already exists")))
        case _ => Success(InternalServerError(Json.obj("error" -> e.getMessage)))
      }
    }
  }

  def login: Action[AnyContent] = (Action andThen authorizationFilter andThen authenticateCredential) { request =>
    val user = request.user
    Ok.withCookies(Cookie("token", Token(user.id.get, user.name).toTokenString)).bakeCookies()
  }

  def createUserForm = Action {
    Ok(views.html.createUser())
  }

  def list: Action[AnyContent] = Action async {
    userService.list().transform {
      case Success(users) => Success(Ok(Json.toJson(users)))
      case Failure(_) => Success(NoContent)
    }
  }

  def find(id: Long): Action[AnyContent] = (Action andThen tokenAuthenticate) async { implicit request =>
    userService.find(id).transform {
      case Success(Some(user)) => Success(Ok(Json.toJson(user)))
      case _ => Success(NoContent)
    }
  }

  def create: Action[User] = Action(validateUserJson) async { request =>
    val user: User = request.body
    userService.create(user)
      .map(user => Ok(Json.toJson(user)))
      .recover {
        case t: Throwable => BadRequest
      }
  }

  private def validateUserJson: BodyParser[User] = parse.json.validate(
    _.validate[User].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  def delete(id: Long): Action[AnyContent] = Action async {
    userService.delete(id).map(_ => Ok).fallbackTo(Future(NoContent))
  }

  def listFollowers: Action[AnyContent] = (Action andThen tokenAuthenticate) async { request =>
    userService.exists(request.token.id).flatMap { _ =>
      userService.listFollowers(request.token.id).map(Json.toJson(_)).map {
        Ok(_)
      }
    }.fallbackTo(Future.successful(BadRequest("user not exists")))
  }
}
