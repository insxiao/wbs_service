package util.extractors

import util.encrypt.Digest

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

/**
  * signed token userId:name:hash(userId:name)
  */
object Token {
  def unapply(token: String): Option[(Long, String)] = token split ":" match {
    case Array(userId, name, hash)
      if Digest.digest(userId + ":" + name) == hash
    => Some(userId.toLong, name)

    case _ => None
  }
}
