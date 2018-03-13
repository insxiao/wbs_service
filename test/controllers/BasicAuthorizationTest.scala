package controllers

import org.scalatest.FunSuite
import org.scalatestplus.play.PlaySpec
import utils.extractors.{BasicAuthorization, Credential}

class BasicAuthorizationTest extends PlaySpec {

  import extensions.Ex.Base64Convert
  private val testUsername = "test#username#1"
  private val testPassword = "test#password#1"

  "Credential" should {

    "extract username & password from `username:password`" in {
      s"$testUsername:$testPassword" match {
        case Credential(username, password) =>
          assert(username == testUsername)
          assert(password == testPassword)
          succeed
        case _ => fail("failed to extract")
      }
    }

    "failed to wrong pattern" in {
      s"$testUsername:$testUsername:$testPassword$testPassword" match {
        case Credential(_, _) => fail("extract username and password from wrong pattern")
        case _ => succeed
      }
    }

  }

  "BasicAuthorization" should {
    "extract username & password" in {
      "Basic " + s"$testUsername:$testPassword".encode match {
        case BasicAuthorization(username, password) =>
          assert(username == testUsername)
          assert(password == testPassword)
          succeed
        case _ => fail("cannot extract from <Basic `username`:`password`>")
      }
    }

    "failed" in {
      "Basic ff:ff" match {
        case BasicAuthorization(_, _) => fail("success with non base64 data")
        case _ => succeed
      }
    }
  }
}
