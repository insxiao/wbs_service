package services

import javax.inject.{Inject, Singleton}

import models.UserRepository
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext

@Singleton
class UserService @Inject()(private val dbConfigProvider: DatabaseConfigProvider)
                           (implicit ec: ExecutionContext)
  extends UserRepository(dbConfigProvider) with AuthenticationService {
  override def userRepository: UserRepository = this

  override implicit def executionContext: ExecutionContext = ec
}
