package util.extractors

import util.encrypt.CredentialDigest

/**
  * 提取器 "username:password" => (username, password)
  */
object Credential {
  // extractor for "username:password"
  def unapply(s: String): Option[(String, String)] = s split ":" match {
    case Array(username, password) => Some(username, password)
    case _ => None
  }
}


