package services

import javax.inject.{Inject, Singleton}

import models.{MicroBlog, Repository}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MicroBlogService @Inject()(val repository: Repository)
                                (implicit ec: ExecutionContext) {

  def post(microBlog: MicroBlog): Future[MicroBlog] = repository.MicroBlogs.create(microBlog)

  def mostRecently(offset: Int, size: Int, userId: Option[Long] = None): Future[Seq[MicroBlog]] = repository.MicroBlogs.mostRecently(offset, size, userId)

  def create(microBlog: MicroBlog): Future[MicroBlog] = repository.MicroBlogs.create(microBlog)

  def find(id: Long): Future[Option[MicroBlog]] = repository.MicroBlogs.findByBlogId(id)

  def search(q: String, offset: Int = 0, size: Int = 10): Future[Seq[MicroBlog]] = repository.MicroBlogs.search(q, offset, size)

  def delete(id: Long): Future[Int] = repository.MicroBlogs.delete(id)
}


