package controllers

import javax.inject.{Inject, Singleton}

import models.{Repository, Token}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsError, Json, Reads}
import play.api.mvc.{AbstractController, BodyParser, ControllerComponents, Cookie}
import services.{AuthenticationService, UserService}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class UserController @Inject()(cc: ControllerComponents)
                              (implicit val executionContext: ExecutionContext, val repository: Repository)
  extends AbstractController(cc) with AuthorizationFunction {

  import models.User

  def register = Action(validateUserJson[User]) async { request =>
    val user = request.body
    repository.Users.create(user).map(user => Json.toJson(user)).map(Ok(_)).recoverWith {
      case ex: Throwable => repository.Users.exists(user.name).transform {
        case Success(true) => Success(BadRequest(Json.obj("reason" -> "user already exists")))
        case _ => Success(ServiceUnavailable)
      }
    }
  }

  private def validateUserJson[User: Reads]: BodyParser[User] = parse.json.validate(
    _.validate[User].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  def login = (Action andThen authorizationFilter andThen authenticateCredential) { request =>
    val user = request.user
    Ok.withCookies(Cookie("token", Token(user.id.get, user.name).toCookieValue)).bakeCookies()
  }

  def createUserForm = Action {
    Ok(views.html.createUser())
  }

  def list = Action async {
    repository.Users.list().transform {
      case Success(users) => Success(Ok(Json.toJson(users)))
      case Failure(_) => Success(NoContent)
    }
  }

  def find(id: Long) = Action async {
    repository.Users.find(id).transform {
      case Success(Some(user)) => Success(Ok(Json.toJson(user)))
      case _ => Success(NoContent)
    }
  }

  def create = Action(validateUserJson[User]) async { request =>
    val user: User = request.body
    repository.Users.create(user)
      .map(user => Ok(Json.toJson(user)))
      .recover {
        case t: Throwable => BadRequest(t.getStackTraceString)
      }
  }

  def delete(id: Long) = Action async {
    repository.Users.delete(id).map(_ => Ok).fallbackTo(Future(NoContent))
  }
}
