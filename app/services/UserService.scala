package services

import javax.inject.{Inject, Singleton}

import models.Repository
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext

@Singleton
class UserService @Inject()(override val repository: Repository)
                           (implicit ec: ExecutionContext)
  extends AuthenticationService {

  override implicit def executionContext: ExecutionContext = ec
}
