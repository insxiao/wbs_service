package util.encrypt

import java.security.MessageDigest

import play.api.{Configuration, Environment}

/**
  * calculate sha1 with salt
  * sha1 = sha1(s + salt)
  */
object CredentialDigest {

  import util.Hex._

  private val salt = Configuration.load(Environment.simple()).get[String]("hash.salt")

  def digest(s: String, salt: Option[String] = Option(CredentialDigest.salt)): String = {
    val sha1Digest = MessageDigest.getInstance("sha1")
    val saltedData = if (s.endsWith(":")) s + salt.getOrElse("") else s + ":" + salt.getOrElse("")
    sha1Digest.digest(saltedData.getBytes()).toHex
  }
}