package models

import java.time.{LocalDate, LocalDateTime}
import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile

import scala.concurrent.{ExecutionContext, Future}

class UserRepository @Inject()(private val dbConfigProvider: DatabaseConfigProvider)
(implicit ec: ExecutionContext) extends RepositoryImplicits {

  override type Profile = PostgresProfile

  //获取PostgresProfile配置
  val dbConfig = dbConfigProvider.get[PostgresProfile]

  import dbConfig._
  import profile.api._
  import User.{Gender, Male, Female}
  import java.sql

//  /**
//    * 自动转换Gender对象
//    */
//  implicit val genderColumnType: BaseColumnType[Gender] =
//    MappedColumnType.base[Gender, String](
//      gender => if (gender == Male) "M" else "F",
//      s => if (s == "M") Male else Female)
//
//  implicit val localDateColumnType: BaseColumnType[LocalDate] =
//    MappedColumnType.base[LocalDate, sql.Date](
//      ld => if (ld != null) sql.Date.valueOf(ld) else null,
//      date => if (date != null) date.toLocalDate else null)
//
//  implicit val localDateTimeColumnType: BaseColumnType[LocalDateTime] =
//    MappedColumnType.base[LocalDateTime, sql.Timestamp](
//      ldt => if (ldt != null) sql.Timestamp.valueOf(ldt) else null,
//      timestamp => if (timestamp != null) timestamp.toLocalDateTime else null)

  private[UserRepository] val users = TableQuery[UserTable]
  private[UserRepository] val microBlog = TableQuery[MicroBlogTable]
  private[UserRepository] val comments = TableQuery[CommentTable]

  def create(user: User): Future[User] = create(user.name, user.gender, user.password, user.email, user.birthday)

  def create(name: String,
             gender: Gender,
             password: String,
             email: Option[String] = None,
             birthday: Option[LocalDate] = None): Future[User] =
    db.run {
      (users.map(p => (p.name, p.gender, p.password, p.email, p.birthday))
        returning users.map(_.id)
        into ((ut, id) => User(Some(id), ut._1, ut._2, ut._3, Option(ut._4), Option(ut._5)))
        ) += (name, gender, password, email.orNull, birthday.orNull)
    }

  def list(): Future[Seq[User]] = db.run {
    users.result
  }

  def delete(id: Long): Future[Int] = db.run {
    users.filter(_.id === id).delete
  }

  def find(id: Long): Future[Option[User]] = db.run {
    users.filter(_.id === id).result
  } map (_.headOption)

  def find(name: String): Future[Option[User]] = db.run {
    users.filter(_.name === name).result
  } map (_.headOption)

  /**
    * 用户表 Table Row 类
    *
    * @param tag
    */
  private[UserRepository] class UserTable(tag: Tag) extends Table[User](tag, "users") {

    override def * =
      (id.?, name, gender, password, email.?, birthday.?).shaped <> ((User.apply _).tupled, User.unapply)

    def id = column[Long]("user_id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("username")

    def gender = column[Gender]("gender")

    def password = column[String]("password")

    def email = column[String]("email")

    def birthday = column[LocalDate]("birthday")
  }

  /**
    * Table row for micro blog
    */
  private[UserRepository] class MicroBlogTable(tag: Tag) extends Table[MicroBlog](tag, "micro_blog") {
    override def * = (blogId.?, content, postDate, userId) <> ((MicroBlog.apply _).tupled, MicroBlog.unapply)

    def blogId = column[Long]("blog_id", O.PrimaryKey, O.AutoInc)

    def content = column[String]("content")

    def postDate = column[LocalDate]("timestamp")

    def userId = column[Long]("user_id")
  }

  private[UserRepository] class CommentTable(tag: Tag) extends Table[Comment](tag, "COMMENTS") {
    override def * = (id.?, content, stars, userId, timestamp) <> ((Comment.apply _).tupled, Comment.unapply)

    def id = column[Long]("comment_id", O.PrimaryKey, O.AutoInc)

    def content = column[String]("content")

    def stars = column[Int]("stars", O.Default(0))

    def userId = column[Long]("user_id")

    def timestamp = column[LocalDateTime]("timestamp")
  }

}
