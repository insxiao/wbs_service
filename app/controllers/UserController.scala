package controllers

import javax.inject.{Inject, Singleton}

import models.UserRepository
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsError, Json, Reads}
import play.api.mvc.{AbstractController, BodyParser, ControllerComponents}
import services.UserService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class UserController @Inject()(private val userService: UserService, cc: ControllerComponents)
                              (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  import models.User

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
    Future {
      userService.create(user)
    }.flatten
      .map(user => Ok(Json.toJson(user)))
      .fallbackTo(Future.successful(BadRequest))
  }

  private def validateUserJson[User: Reads]: BodyParser[User] = parse.json.validate(
    _.validate[User].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  def delete(id: Long) = Action async {
    userService.delete(id).map(_ => Ok).fallbackTo(Future(NoContent))
  }
}
