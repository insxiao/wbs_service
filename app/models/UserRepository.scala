package models

import java.time.LocalDate
import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile

import scala.concurrent.{ExecutionContext, Future}

class UserRepository @Inject()(private val dbConfigProvider: DatabaseConfigProvider)
                              (implicit ec: ExecutionContext) {
  //获取PostgresProfile配置
  private[UserRepository] val dbConfig = dbConfigProvider.get[PostgresProfile]
  import dbConfig._
  import profile.api._
  import User.{Gender, Male, Female}

  /**
    * 自动转换Gender对象
    */
  implicit val genderColumnType: BaseColumnType[Gender] = MappedColumnType.base[Gender, String](gender => if (gender == Male) "M" else "F", s => if (s == "M") Male else Female)
  implicit val localDateColumnType: BaseColumnType[LocalDate] = MappedColumnType.base[LocalDate, java.sql.Date](
    java.sql.Date.valueOf(_),
    _.toLocalDate
  )
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

  private[UserRepository] val user = TableQuery[UserTable]

  /**
    * Table row for micro blog
    */
  private[UserRepository] class MicroBlogTable(tag: Tag) extends Table[MicroBlog](tag, "micro_blog") {
    def blogId = column[Long]("blog_id", O.PrimaryKey, O.AutoInc)

    def content = column[String]("content")

    def postDate = column[LocalDate]("timestamp")

    def userId = column[Long]("user_id")

    override def * = (blogId.?, content, postDate, userId) <> ((MicroBlog.apply _).tupled, MicroBlog.unapply)
  }

  private[UserRepository] val microBlog = TableQuery[MicroBlogTable]


  private[UserRepository] class CommentTable(tag: Tag) extends Table[Comment](tag, "COMMENTS") {
    def id = column[Long]("comment_id", O.PrimaryKey, O.AutoInc)
    def content = column[String]("content")
    def stars = column[Int]("stars", O.Default(0))
    def userId = column[Long]("user_id")
    override def * = (id.?, content, stars, userId) <> ((Comment.apply _).tupled, Comment.unapply)
  }

  private[UserRepository] val comment = TableQuery[CommentTable]

  def create(name: String,
             gender: Gender,
             password: String,
             email: Option[String] = None,
             birthday: Option[LocalDate] = None) : Future[User] =
    db.run {
      (user.map(p => (p.name, p.gender, p.password, p.email, p.birthday))
        returning user.map(_.id)
        into ((ut, id) => User(Some(id), ut._1, ut._2, ut._3, Option(ut._4), Option(ut._5)))
        ) += (name, gender, password, email.orNull, birthday.orNull)
    }

  def create(user: User): Future[User] = create(user.name, user.gender, user.password, user.email, user.birthday)

  def list(): Future[Seq[User]] = db.run {
    user.result
  }

  def delete(id: Long): Future[Int] = db.run {
    user.filter(_.id === id).delete
  }

  def find(id: Long): Future[Option[User]] = db.run {
    user.filter(_.id === id).result
  } map (_.headOption)
}
