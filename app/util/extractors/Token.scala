package util.extractors

import util.encrypt.Digest

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
