package controllers

import javax.inject._

import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._
import play.api.mvc._
import services.UserService
import slick.dbio.DBIOAction

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import slick.jdbc.PostgresProfile._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(private val dbConfigProvider: DatabaseConfigProvider, cc: ControllerComponents)
                              (implicit val executionContext: ExecutionContext, val userService: UserService)
  extends AbstractController(cc) with AuthorizationFunction {


  lazy val db = dbConfigProvider.get.db

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

  def loadDb(): Action[AnyContent] = Action async { implicit request: Request[AnyContent] =>
    import slick.jdbc.PostgresProfile.api._
    val currentDate = sql"SELECT CURRENT_TIMESTAMP ".as[java.sql.Timestamp]
    db.run(currentDate).map(v => v(0)).transform {
      case Success(s) => Success(Ok(s.toString))
      case Failure(_) => Success(NoContent)
    }
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

  def testUser = (Action andThen authorizationFilter andThen userFilter) {
    request =>
      Ok(Json.toJson(request.user))
  }
}
