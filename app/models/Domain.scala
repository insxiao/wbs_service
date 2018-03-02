
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global

package models {

  trait Model {

    class User(tag: Tag) extends Table[(Int, String)](tag, "USERS") {
      // TODO specific columns...
      def id = column[Int]("USER_ID", O.PrimaryKey)

      def name = column[String]("USER_NAME")

      override def * = (id, name)
    }

    val users = TableQuery[User]
  }

  object Model extends Model

}