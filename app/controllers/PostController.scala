package controllers

import java.time.LocalDateTime

import javax.inject.{Inject, Singleton}
import models.body.PostResponse
import models.{Comment, MicroBlog, Repository}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsError, JsValue, Json, Reads}
import play.api.mvc._
import services.{CommentService, MicroBlogService, UserService}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class PostController @Inject()(cc: ControllerComponents)
                              (implicit val executionContext: ExecutionContext,
                               val microBlogService: MicroBlogService,
                               val commentService: CommentService)
  extends AbstractController(cc) with AuthorizationFunction {
  def postBlog: Action[MicroBlog] = (Action(validateBlogJson) andThen tokenAuthenticate andThen tokenTransformer) async { request =>
    val blog = request.body

    val user = request.user

    microBlogService.post(blog)

    Future.successful(Ok)
  }

  /**
    * 验证请求体
    *
    * @tparam MicroBlog [[models.MicroBlog]]
    * @return
    */
  private def validateBlogJson: BodyParser[MicroBlog] = parse.json.validate(
    _.validate[MicroBlog].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  def top(offset: Int, size: Int, userId: Option[Long]) = Action async { request =>
    microBlogService.mostRecently(offset, size)
      .map { blogs =>
        PostResponse(blogs, routes.PostController.top(offset + size, size, None).url)
      }
      .map (Json.toJson(_))
      .map (Ok(_))
      .recover {
        case e: Throwable => InternalServerError
      }
  }

  /**
    * create blog with user id
    * @return
    */
  def create = (Action(validateBlogJson) andThen tokenAuthenticate) async { request =>
    val microBlog = request.body
    val token = request.token
    if (token.id == microBlog.userId) {
      microBlogService.create(microBlog)
        .map(Json.toJson(_))
        .map(Ok(_))
        .recover { case _ => InternalServerError }
    }
    else Future.successful(Unauthorized)
  }

  def find(id: Long) = Action async {
    microBlogService.find(id).transform {
      case Success(Some(post)) => Success(Ok(Json.toJson(post)))
      case Success(None) => Success(NoContent)
      case _ => Failure(new NoSuchElementException)
    }.recover { case _ => InternalServerError }
  }

  /**
    * 验证请求体并转换为Comment
    */
  val validateCommentJson: BodyParser[Comment] = parse.json.validate {
    js =>
      val id = (js \ "id").asOpt[Long]
      val blogIdOpt = (js \ "blogId").asOpt[Long]
      val contentOpt = (js \ "content").asOpt[String]
      val starsOpt = (js \ "stars").asOpt[Int]
      val userIdOpt = (js \ "userId").asOpt[Long]
      val tsOpt = (js \ "timestamp").asOpt[LocalDateTime]

      if (Set(contentOpt, blogIdOpt, userIdOpt).contains(None)) Left(UnprocessableEntity)
      else (id, blogIdOpt, contentOpt, starsOpt, userIdOpt, tsOpt) match {
        case (_, Some(blogId), Some(content), Some(stars), Some(userId), Some(ts)) =>
          Right(Comment(id, blogId, content, stars, userId, ts))
      }
  }

  /**
    * Comment a micro blog
    * @return Future of Result
    */
  def comment: Action[Comment] = (Action(validateCommentJson) andThen tokenAuthenticate) async { request =>
    commentService.comment(request.body)
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover {
        case e: Throwable => InternalServerError(Json.obj("exception" -> e.getMessage))
      }
  }
}
