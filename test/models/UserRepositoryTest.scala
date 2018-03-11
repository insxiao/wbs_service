package models

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Injecting

import scala.util.{Failure, Success}

class UserRepositoryTest extends PlaySpec with GuiceOneAppPerSuite with Injecting {
  private val repository: UserRepository = inject[UserRepository]
  import scala.concurrent.ExecutionContext.Implicits.global

  "repository" must {
    "return user after create" in {
      val xiao = repository.create("xiao", Male, "123456", None, None)
      xiao.onComplete {
        case Success(user) => println(user)
        case Failure(ex) => println(ex.toString)
      }
    }

    "return affected row num after delete" in {
      repository.delete(3).onComplete {
        case Success(0) => println("failure")
        case Success(_) => println("success")
        case Failure(_) => println("failure")
      }
    }
  }
}
