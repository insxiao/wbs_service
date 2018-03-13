package controllers

import javax.inject.{Inject, Singleton}

import models.UserRepository
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsError, Json, Reads}
import play.api.mvc.{AbstractController, BodyParser, ControllerComponents}
import services.{AuthenticationService, UserService}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class UserController @Inject()(cc: ControllerComponents)
                              (implicit val executionContext: ExecutionContext, val userService: UserService)
  extends AbstractController(cc) with AuthorizationFunction {

  import models.User

  def register = Action(validateUserJson[User]) async { request =>
    val user = request.body
    userService.create(user).map(user => Json.toJson(user)).map(Ok(_)).recover {
      case _ => BadRequest(Json.obj("status" -> "failed"))
    }

    Future { Ok }
  }

  private def validateUserJson[User: Reads]: BodyParser[User] = parse.json.validate(
    _.validate[User].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  def login = (Action andThen authorizationFilter andThen authenticateCredential) { request =>
    val user = request.user
    Ok.withSession("username" -> user.name, "user_id" -> user.id.toString)
  }

  def createUserForm = Action {
    Ok(views.html.createUser())
  }

  def list = Action async {
    userService.list().transform {
      case Success(users) => Success(Ok(Json.toJson(users)))
      case Failure(_) => Success(NoContent)
    }
  }

  def find(id: Long) = Action async {
    userService.find(id).transform {
      case Success(Some(user)) => Success(Ok(Json.toJson(user)))
      case _ => Success(NoContent)
    }
  }

  def create = Action(validateUserJson[User]) async { request =>
    val user: User = request.body
    userService.create(user)
      .map(user => Ok(Json.toJson(user)))
      .recover {
        case t: Throwable => BadRequest(t.getStackTraceString)
      }
  }

  def delete(id: Long) = Action async {
    userService.delete(id).map(_ => Ok).fallbackTo(Future(NoContent))
  }
}
