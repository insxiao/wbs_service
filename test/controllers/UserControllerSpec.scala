package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.{Application, Configuration, Logger}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test._
import play.api.test.Helpers._

class UserControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  val logger = Logger(classOf[UserControllerSpec])
  override def fakeApplication(): Application =
    GuiceApplicationBuilder(configuration = Configuration(
      "slick.dbs.default.profile" -> "slick.jdbc.H2Profile$",
      "slick.dbs.default.db.driver" -> "org.h2.Driver",
      "slick.dbs.default.db.url" -> "jdbc:h2:mem:weibos"
    )).build()

  "UserController GET" should {
    "Not Supported Action" in {
      val controller = inject[UserController]
      val delete = controller.delete(-1).apply(FakeRequest("POST", "/user"))
      status(delete) mustBe NO_CONTENT
    }
  }
}
