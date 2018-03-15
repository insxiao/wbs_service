package services

import javax.inject.Inject

import models.{MicroBlog, Repository}

import scala.concurrent.ExecutionContext

@Singleton()
class MicroBlogService @Inject()(val repository: Repository)
                                (implicit ec: ExecutionContext) {

  def post(microBlog: MicroBlog) = repository.MicroBlogs.create(microBlog)
}


