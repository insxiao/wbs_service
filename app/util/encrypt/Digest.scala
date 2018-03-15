package util.encrypt

import java.security.MessageDigest

import play.api.{Configuration, Environment}
object Digest {
  import util.Hex._

  private val salt = Configuration.load(Environment.simple()).get[String]("hash.salt")

  def digest(s: String, salt: Option[String] = Option(Digest.salt)): String = {
    val sha1Digest = MessageDigest.getInstance("sha1")
    val saltedData = s + salt.getOrElse("")
    sha1Digest.digest(saltedData.getBytes()).toHex
  }
}