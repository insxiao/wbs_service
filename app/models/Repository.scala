package models

import java.time.{LocalDate, LocalDateTime}

import javax.inject.Inject
import models._
import models.User.Gender
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class Repository @Inject()(val dbConfigProvider: DatabaseConfigProvider)
                          (implicit val executionContext: ExecutionContext)
  extends RepositoryImplicits {
  self =>
  type Profile = PostgresProfile
  val dbConfig = dbConfigProvider.get[Profile]

  import dbConfig._
  import profile.api._

  private val users = TableQuery[UserTable]
  private val microBlogs = TableQuery[MicroBlogTable]
  private val comments = TableQuery[CommentTable]
  private val follows = TableQuery[FollowTable]

  /**
    * TableRow for User schema
    *
    * @param tag
    */
  private[Repository] class UserTable(tag: Tag) extends Table[User](tag, "users") {

    override def * =
      (id.?, name, gender, password, email.?, birthday.?, avatar.?).shaped <> ((User.apply _).tupled, User.unapply)

    def id = column[Long]("user_id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("username")

    def gender = column[Gender]("gender")

    def password = column[String]("password")

    def email = column[String]("email")

    def birthday = column[LocalDate]("birthday")

    def avatar = column[String]("avatar")
  }

  /**
    * TableRow for MicroBlog schema
    *
    * @param tag
    */
  private[Repository] class MicroBlogTable(tag: Tag) extends Table[MicroBlog](tag, "micro_blog") {
    override def * = (blogId.?, content, timestamp, userId) <> ((MicroBlog.apply _).tupled, MicroBlog.unapply)

    def blogId = column[Long]("blog_id", O.PrimaryKey, O.AutoInc)

    def content = column[String]("content")

    def timestamp = column[LocalDateTime]("timestamp")

    def userId = column[Long]("user_id")
  }

  /**
    * TableRow for Comment schema
    *
    * @param tag
    */
  private[Repository] class CommentTable(tag: Tag) extends Table[Comment](tag, "comments") {
    override def * = (id.?, blogId, content, stars, userId, timestamp) <> ((Comment.apply _).tupled, Comment.unapply)

    /**
      * primary key
      */
    def id = column[Long]("comment_id", O.PrimaryKey, O.AutoInc)

    def blogId = column[Long]("blog_id")

    def content = column[String]("content")

    def stars = column[Int]("stars", O.Default(0))

    def userId = column[Long]("user_id")

    def timestamp = column[LocalDateTime]("timestamp")
  }

  /**
    * 关注表
    *
    * @param tag
    */
  private[Repository] class FollowTable(tag: Tag) extends Table[(Long, Long, LocalDateTime)](tag, "follows") {
    def * = (userId, followerId, timestamp)

    def userId = column[Long]("user_id")

    def followerId = column[Long]("follower_id")

    def timestamp = column[LocalDateTime]("timestamp")
  }

  /**
    * user table repository object
    */
  object Users {
    val users = self.users

    def search(q: String, offset: Int, size: Int): Future[Seq[User]] = db.run {
      users.filter(u => u.name.like(s"%$q%") || u.id === Try(q.toLong)
        .getOrElse(-1L))
        .sortBy(_.name.asc)
        .drop(offset)
        .take(size)
        .result
    }

    def create(user: User): Future[User] =
      create(user.name, user.gender, user.password, user.email, user.birthday, user.avatar)

    def create(name: String,
               gender: Gender,
               password: String,
               email: Option[String] = None,
               birthday: Option[LocalDate] = None,
               avatar: Option[String] = None): Future[User] =
      db.run {
        (users.map(p => (p.name, p.gender, p.password, p.email, p.birthday, p.avatar))
          returning users.map(_.id)
          into ((ut, id) => User(Some(id), ut._1, ut._2, ut._3, Option(ut._4), Option(ut._5), Option(ut._6)))
          ) += (name, gender, password, email.orNull, birthday.orNull, avatar.orNull)
      }

    def list(): Future[Seq[User]] = db.run {
      users.result
    }

    def delete(id: Long): Future[Int] = db.run {
      users.filter(_.id === id).delete
    }

    def delete(username: String): Future[Int] = db.run {
      users.filter(_.name === username).delete
    }

    def find(id: Long): Future[Option[User]] = db.run {
      users.filter(_.id === id).result
    } map (_.headOption)

    def find(name: String): Future[Option[User]] = db.run {
      users.filter(_.name === name).result
    } map (_.headOption)


    def exists(id: Long): Future[Boolean] = db.run {
      users.filter(_.id === id).exists.result
    }

    def exists(name: String): Future[Boolean] = db.run {
      users.filter(_.name === name).exists.result
    }

    def changePassword(id: Long, password: String): Future[Int] = db.run {
      users.filter(_.id === id).map(_.password).update(password)
    }

    def listFollowers(id: Long): Future[Seq[User]] = Future {
      for {
        follow <- follows
        if follow.userId === id
        user <- users if user.id === follow.followerId
      } yield user
    }.flatMap(q => db.run(q.result))

    def listFollows(id: Long): Future[Seq[User]] = Future {
      for {
        follow <- follows
        if follow.followerId === id
        user <- users if user.id === follow.userId
      } yield user
    }.flatMap(q => db.run(q.result))
  }

  /**
    * MicroBlog table repository object
    */
  object MicroBlogs {
    val microBlogs = self.microBlogs

    def search(q: String, offset: Int, size: Int): Future[Seq[MicroBlog]] = db.run {
      microBlogs.filter(p => p.content.like(s"%$q%"))
        .sortBy(_.timestamp.desc)
        .drop(offset)
        .take(size)
        .result
    }

    def create(microBlog: MicroBlog): Future[MicroBlog] = db.run {
      (microBlogs.map(mb => (mb.content, mb.timestamp, mb.userId))
        returning microBlogs.map(_.blogId)
        into { case ((content, timestamp, userId), blogId) => MicroBlog(Some(blogId), content, timestamp, userId) }
        ) += (microBlog.content, microBlog.timestamp, microBlog.userId)
    }


    def findByUserId(userId: Long): Future[Seq[MicroBlog]] = db.run {
      microBlogs.filter(_.userId === userId).sortBy(_.timestamp).result
    }

    def findByBlogId(blogId: Long): Future[Option[MicroBlog]] = db.run {
      microBlogs.filter(_.blogId === blogId).result
    }.map(_.headOption)

    def delete(id: Long): Future[Int] = db.run {
      microBlogs.filter(_.blogId === id).delete
    }

    def mostRecently(offset: Int, size: Int, userId: Option[Long], followerId: Option[Long]): Future[Seq[MicroBlog]] = db.run {
      userId match {
        case Some(id) =>
          Logger.debug(s"most recently post with userId $id")
          microBlogs.filter(_.userId === id).sortBy(_.timestamp.desc).drop(offset).take(size).result
        case None => microBlogs.sortBy(_.timestamp.desc).drop(offset).take(size).result
      }

      ((userId, followerId) match {
        case (None, None) => microBlogs
        case (Some(uid), _) => microBlogs
          .filter(_.userId === uid)
        case (None, Some(fid)) =>
          for {
            follow <- follows if follow.followerId === fid
            post <- microBlogs if post.userId === follow.userId
          } yield post
      }).sortBy(_.timestamp.desc)
        .drop(offset)
        .take(size)
        .result
    }
  }

  /**
    * Comment 表repository对象
    */
  object Comments {
    val comments = self.comments

    def create(comment: Comment): Future[Comment] = db.run {
      (comments.map(c => (c.blogId, c.content, c.userId))
        returning comments.map(c => (c.id, c.stars, c.timestamp))
        into { case ((blogId, content, userId), (commentId, stars, timestamp)) =>
        Comment(Some(commentId), blogId, content, stars, userId, timestamp)
      }
        ) += (comment.blogId, comment.content, comment.userId)
    }

    def findByBlogId(id: Long): Future[Seq[Comment]] = db.run {
      comments.filter(_.id === id).sortBy(_.timestamp.desc).result
    }

    def findByBlogId(id: Long, offset: Int, size: Int): Future[Seq[Comment]] = db.run {
      comments.filter(_.blogId === id).sortBy(_.timestamp.desc).drop(offset).take(size).result
    }

    def delete(id: Long): Future[Int] = db.run {
      comments.filter(_.id === id).delete
    }

    def find(id: Long): Future[Option[Comment]] = db.run {
      comments.filter(_.id === id).result
    }.map(_.headOption)
  }

  object Follows {
    val follows = TableQuery[FollowTable]

    def get(userId: Long, followerId: Long): Future[Option[User]] = db.run {
      follows.filter(f => f.userId === userId && f.followerId === followerId)
        .flatMap(f => users.filter(_.id === f.userId)).result.headOption
    }

    def exists(userId: Long, followerId: Long): Future[Boolean] = db.run {
      follows.filter(f => f.userId === userId && f.followerId === followerId)
        .exists
        .result
    }

    def follow(userId: Long, followedId: Long): Future[(Long, Long, LocalDateTime)] = db.run {
      (follows.map(f => (f.userId, f.followerId))
        returning follows.map(_.timestamp)
        into { case ((userId, followId), timestamp) => (userId, followId, timestamp) }
        ) += (followedId, userId)
    }

    def listFollowers(userId: Long): Future[Seq[(Long, Long, LocalDateTime)]] = db.run {
      follows.filter(_.userId === userId).sortBy(_.timestamp.desc).result
    }

    def delete(userId: Long, followedId: Long): Future[Int] = db.run {
      follows.filter(f => f.userId === followedId && f.followerId === userId).delete
    }
  }
}
