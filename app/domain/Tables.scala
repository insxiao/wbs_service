import java.sql.Date

import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape}

class Users(tag: Tag) extends Table[(Long, String)](tag, "USERS") {
  def id = column[Long]("USER_ID", O.PrimaryKey)

  def name = column[String]("USER_NAME")

  override def * : ProvenShape[(Long, String)] = (id, name)
}

class MicroBlog(tag: Tag) extends Table[(Long, Long, String, Date)](tag, "MICRO_BLOG") {
  def blogId = column[Long]("BLOG_ID", O.PrimaryKey)

  def userId = column[Long]("USER_ID")

  def content = column[String]("CONTENT")

  def postDate = column[Date]("POST_DATE")

  override def * : ProvenShape[(Long, Long, String, Date)] = (blogId, userId, content, postDate)

  def user: ForeignKeyQuery[Users, (Long, String)] = foreignKey("USER_ID", userId, TableQuery[Users])(_.id)
}


