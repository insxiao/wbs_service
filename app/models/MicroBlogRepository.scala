package models

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MicroBlogRepository @Inject()(private val dbConfigProvider: DatabaseConfigProvider)
                                   (implicit ec: ExecutionContext) extends RepositoryImplicits {

  override type Profile = PostgresProfile

  override val dbConfig = dbConfigProvider.get[Profile]

  import dbConfig._
  import profile.api._

  private val microBlogs = TableQuery[MicroBlogTable]


  def create(microBlog: MicroBlog): Future[MicroBlog] = db.run {
    (microBlogs.map(mb => (mb.content, mb.timestamp, mb.userId))
      returning microBlogs.map(_.id)
      into { case ((content, timestamp, userId), idOpt) => MicroBlog(idOpt, content, timestamp, userId) }
      ) += (microBlog.content, microBlog.timestamp, microBlog.userId)
  }

  def findByUserId(userId: Long): Future[Seq[MicroBlog]] = db.run {
    microBlogs.filter(_.userId == userId).sortBy(_.timestamp).result
  }

  def findByBlogId(blogId: Long): Future[Option[MicroBlog]] = db.run {
    microBlogs.filter(_.blogId === blogId).result
  }.map(_.headOption)

  def delete(id: Long): Future[Int] = db.run {
    microBlogs.filter(_.blogId == id).delete
  }

  /**
    * Table row for micro blog
    */
  class MicroBlogTable(tag: Tag) extends Table[MicroBlog](tag, "micro_blog") {
    override def * = (blogId.?, content, timestamp, userId) <> ((MicroBlog.apply _).tupled, MicroBlog.unapply)

    def blogId = column[Long]("blog_id", O.PrimaryKey, O.AutoInc)

    def content = column[String]("content")

    def timestamp = column[LocalDate]("timestamp")

    def userId = column[Long]("user_id")
  }

}
