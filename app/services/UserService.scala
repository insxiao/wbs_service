package services

import javax.inject.Inject

import models.UserRepository
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext

class UserService @Inject()(private val dbConfigProvider: DatabaseConfigProvider)
                           (implicit ec: ExecutionContext)
  extends UserRepository(dbConfigProvider)
