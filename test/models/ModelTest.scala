package models

import org.scalatest._

class ModelTest extends FlatSpec {

  "users" should "println" in {
    println(Model.users)
  }

}
