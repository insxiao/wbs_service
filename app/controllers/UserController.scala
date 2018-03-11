package controllers

import javax.inject.{Inject, Singleton}

import models.UserRepository
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import services.UserService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class UserController @Inject()(private val userService: UserService, cc: ControllerComponents)
                              (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def createUserForm = Action {
    Ok(views.html.createUser())
  }

  def list = Action async  {
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
}
