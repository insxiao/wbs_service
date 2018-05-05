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

  def exists(name: String) = Action async userService.exists(name).transform {
    case Success(true) => Success(Ok("exists"))
    case Success(false) => Success(Ok("not exists"))
    case _ => Success(InternalServerError)
  }

  def login: Action[AnyContent] = (Action andThen authorizationFilter andThen authenticateCredential) { request =>
    val user = request.user
    Ok(Json.toJson(user)).withCookies(Cookie("token", Token(user.id.get, user.name).toTokenString)).bakeCookies()
  }

  def createUserForm = Action {
    Ok(views.html.createUser())
  }

  def list: Action[AnyContent] = Action async {
    userService.list().transform {
      case Success(users) => Success(Ok(Json.toJson(users)))
      case Failure(_) => Success(Ok(Json.toJson(List.empty[User])))
    }
  }

  def find(id: Long): Action[AnyContent] = Action async { implicit request =>
    logger.info(s"${request.remoteAddress} fetch user info with user id $id")
    userService.find(id).transform {
      case Success(Some(user)) => Success(Ok(Json.obj("id" -> user.id,
        "name" -> user.name,
        "gender" -> user.gender,
        "email" -> user.email,
        "birthday" -> user.birthday,
        "avatar" -> user.avatar)))
      case _ => Success(NoContent)
    }
  }

  def create: Action[User] = Action(validateUserJson) async { request =>
    val user: User = request.body
    userService.create(user)
      .map(user => Ok(Json.toJson(user)))
      .recoverWith({
        case t: Throwable =>
          logger.warn(s"${request.remoteAddress} error ${t.getMessage}")
          userService.exists(user.name).transform {
            case Success(true) => Success(Conflict(Json.obj("reason" -> s"user ${user.name} already exists")))
            case _ => Success(InternalServerError(t.getMessage))
          }
      })
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
