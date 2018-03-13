package models

import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext

@Singleton
class MicroBlogRepository@Inject()(private val dbConfigProvider: DatabaseConfigProvider)
                                  (implicit ec: ExecutionContext) {

}
