package services

import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext

class MicroBlogService@Inject()(private val dbConfigProvider: DatabaseConfigProvider)
                               (implicit ec: ExecutionContext) {

}
