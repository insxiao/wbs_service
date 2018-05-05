package controllers

import javax.inject.{Inject, Singleton}
import models.body.Next
import play.api.Logger
import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc.{AbstractController, ControllerComponents, RequestHeader}
import services.{CommentService, MicroBlogService, UserService}

import scala.util.{Failure, Success, Try}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SearchController @Inject()(cc: ControllerComponents)
                                (implicit val executionContext: ExecutionContext,
                                 val microBlogService: MicroBlogService,
                                 override val userService: UserService,
                                 val commentService: CommentService)
  extends AbstractController(cc) with AuthorizationFunction {

  val logger = Logger(classOf[SearchController])

  case class SearchResponse[T](data: Seq[T], next: Next)

  object SearchResponse {
    implicit def SearchResponseWrites[T](implicit fmt: Writes[T]) = Json.writes[SearchResponse[T]]
    def apply[T](data: Seq[T], q: String, `type`: String, offset: Int, size: Int)(implicit requestHeader: RequestHeader): SearchResponse[T] =
      SearchResponse(data, Next(
        routes.SearchController.search(q, `type`, offset + size, size).relative,
        Map("q" -> q, "type" -> `type`, "offset" -> offset, "size" -> size)
      ))
  }


  def search(q: String, `type`: String, offset: Int, size: Int) = Action async { implicit request =>
    logger.debug(s"$q, ${`type`}")
    `type` match {
      case "user" => userService.search(q, offset, size).transform {
        case Success(Seq()) => Success(NoContent)
        case Success(seq) => Success(
          Ok(Json.toJson(SearchResponse(seq, q, `type`, offset, size))))
        case Failure(e) => logger.warn(e.getMessage)
          Success(InternalServerError)
      }
      case "post" => microBlogService.search(q, offset, size).transform {
        case Success(Seq()) => Success(NoContent)
        case Success(r) => Success(Ok(Json.toJson(SearchResponse(r, q, `type`, offset, size))))
        case Failure(e) => logger.warn(e.getMessage)
          Success(InternalServerError)
      }
    }
  }
}
