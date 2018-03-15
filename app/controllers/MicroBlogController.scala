package controllers

import javax.inject.{Inject, Singleton}

import models.Repository
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{CommentService, UserService}

import scala.concurrent.ExecutionContext

@Singleton
class MicroBlogController @Inject()(cc: ControllerComponents)
                                   (implicit val executionContext: ExecutionContext, val commentService: CommentService)
  extends AbstractController(cc) with AuthorizationFunction {

  def postBlog = (Action and )
}
