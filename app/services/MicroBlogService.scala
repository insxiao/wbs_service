package services

import javax.inject.{Inject, Singleton}

import models.{MicroBlog, Repository}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MicroBlogService @Inject()(val repository: Repository)
                                (implicit ec: ExecutionContext) {

  def post(microBlog: MicroBlog): Future[MicroBlog] = repository.MicroBlogs.create(microBlog)

  def mostRecently(offset: Int, size: Int): Future[Seq[MicroBlog]] = repository.MicroBlogs.mostRecently(offset, size)

  def create(microBlog: MicroBlog): Future[MicroBlog] = repository.MicroBlogs.create(microBlog)

  def find(id: Long): Future[Option[MicroBlog]] = repository.MicroBlogs.findByBlogId(id)
}


