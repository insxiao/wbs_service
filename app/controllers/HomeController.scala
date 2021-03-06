package controllers

import javax.inject._

import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._
import play.api.mvc._
import services.{AuthenticationService, UserService}
import slick.dbio.DBIOAction

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import slick.jdbc.PostgresProfile._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents)
                              (implicit val executionContext: ExecutionContext, override val userService: UserService)
  extends AbstractController(cc) with AuthorizationFunction {

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def allUsers() = Action async { implicit request: Request[AnyContent] =>
    Future {
      Ok
    }
  }

  def addUser(username: String, gender: String, password: String, email: String, birthday: String): Unit = {
    DBIOAction
  }

  def testAuthRefiner = (Action andThen authorizationFilter) { request =>
    Ok(Json.obj("username" -> request.username, "password" -> request.password))
  }

  def testUser = (Action andThen authorizationFilter andThen authenticateCredential) {
    request =>
      Ok(Json.toJson(request.user))
  }

}
