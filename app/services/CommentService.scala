package services

import javax.inject.Inject

import models.{Comment, Repository}

import scala.concurrent.{ExecutionContext, Future}

class CommentService @Inject() (val repository: Repository)
                     (implicit ec: ExecutionContext) {

  def comment(comment: Comment): Future[Comment] = repository.Comments.create(comment)

}
