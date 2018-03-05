import java.util.Date

object Gender extends Enumeration {
  val Male, Female = VALUE
}

case class User(id: Long,
                name: String,
                birthday: Date,
                email: String,
                phone: String)


case class MicroBlog(id: Long, content: String, postDate: Date, ownerId: Long)