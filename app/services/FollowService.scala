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

  def unfollow()(me: User, other: User): Future[Int] = repository.Follows.delete(me.id.get, other.id.get)

  def listFollowers(id: Long): Future[Seq[User]] = repository.Users.listFollowers(id)

  def listFollows(id: Long): Future[Seq[User]] = repository.Users.listFollows(id)

  override implicit def executionContext: ExecutionContext = ec
}
