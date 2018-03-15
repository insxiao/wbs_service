package controllers

import javax.inject.{Inject, Singleton}

import models.{MicroBlog, Repository}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsError, Reads}
import play.api.mvc._
import services.{CommentService, MicroBlogService, UserService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MicroBlogController @Inject()(cc: ControllerComponents)
                                   (implicit val executionContext: ExecutionContext, val microBlogService: MicroBlogService)
  extends AbstractController(cc) with AuthorizationFunction {
  /**
    * 验证请求体
    * @tparam MicroBlog [[models.MicroBlog]]
    * @return
    */
  private def validateBlogJson[MicroBlog: Reads]: BodyParser[MicroBlog] = parse.json.validate(
    _.validate[MicroBlog].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  def postBlog: Action[MicroBlog] = (Action(validateBlogJson) andThen tokenAuthenticate andThen tokenTransformer) async { request =>
    val blog = request.body

    val user = request.user



    Future.successful(Ok)
  }
}
