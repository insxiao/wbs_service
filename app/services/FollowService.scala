package services

import java.time.LocalDateTime

import javax.inject.{Inject, Singleton}
import models.{Repository, User}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FollowService @Inject()(override val repository: Repository)
                           (implicit ec: ExecutionContext)
  extends AuthenticationService {

  def follow(me: User, other: User): Future[(Long, Long, LocalDateTime)] = repository.Follows.follow(me.id.get, other.id.get)

  def follow(me: Long, other: Long): Future[(Long, Long, LocalDateTime)] = repository.Follows.follow(me, other)

  def unfollow(me: User, other: User): Future[Int] = repository.Follows.delete(me.id.get, other.id.get)

  def unfollow(me: Long, other: Long): Future[Int] = repository.Follows.delete(me, other)

  def listFollowers(id: Long): Future[Seq[User]] = repository.Users.listFollowers(id)

  def listFollows(id: Long): Future[Seq[User]] = repository.Users.listFollows(id)

  def exists(userId: Long, followerId: Long): Future[Boolean] = repository.Follows.exists(userId, followerId)

  def get(userId: Long, followerId: Long): Future[Option[User]] = repository.Follows.get(userId, followerId)

  override implicit def executionContext: ExecutionContext = ec
}
