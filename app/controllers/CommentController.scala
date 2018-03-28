package controllers

import javax.inject.{Inject, Singleton}
import models.Comment
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{CommentService, MicroBlogService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CommentController @Inject()(cc: ControllerComponents)
                                 (implicit val executionContext: ExecutionContext,
                                  val microBlogService: MicroBlogService,
                                  val commentService: CommentService)
  extends AbstractController(cc) with AuthorizationFunction {

  def find(id: Long) = Action async {
    commentService.find(id)
      .map {
        case Some(comment) => Ok(Json.toJson(comment))
        case _ => NoContent
      }.recover {
      case e: Throwable => InternalServerError(Json.obj("error" -> e.getMessage))
    }
  }

  def allComments(blogId: Long, offset: Int, size: Int) = Action async { request =>
    def responseWithNext(comments: Seq[Comment]) = {
      Json.obj(
        "comments" -> comments,
        "next" -> routes.CommentController.allComments(blogId, offset + size, size).url
      )
    }

    commentService.findByBlogId(blogId, offset, size)
      .map(responseWithNext)
      .map(Ok(_))
      .recover {
        case e: Throwable => InternalServerError(Json.obj("error" -> e.getMessage))
      }
  }
}