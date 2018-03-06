import java.sql.Date

import domain.{Gender, Male, Female}

import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape}


object Tables {

  implicit val genderColumnType = MappedColumnType.base[Gender, String](gender => if (gender == Male) "M" else "F", s => if (s == "M") Male else Female)

  /**
    * Users table row class
    */
  class Users(tag: Tag) extends Table[(Long, String, Gender, String, String, Date)](tag, "USERS") {
    def id = column[Long]("USER_ID", O.PrimaryKey)

    def name = column[String]("USERNAME")

    def gender = column[Gender]("GENDER")

    def password = column[String]("PASSWORD")

    def email = column[String]("EMAIL")

    def birthday = column[Date]("BIRTHDAY")

    override def * : ProvenShape[(Long, String, Gender, String, String, Date)] = (id, name, gender, password, email, birthday)
  }

  /**
    * Table row for micro blog
    */
  class MicroBlog(tag: Tag) extends Table[(Long, Long, String, Date)](tag, "MICRO_BLOG") {
    override def * : ProvenShape[(Long, Long, String, Date)] = (blogId, userId, content, postDate)

    def blogId = column[Long]("BLOG_ID", O.PrimaryKey)

    def content = column[String]("CONTENT")

    def postDate = column[Date]("POST_DATE")

    def user: ForeignKeyQuery[Users, (Long, String, Gender, String, String, Date)] = foreignKey("USER_ID", userId, TableQuery[Users])(_.id)

    def userId = column[Long]("USER_ID")
  }
}