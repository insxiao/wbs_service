package utils.extractors

object BasicAuthorization {
  def unapply(arg: String): Option[(String, String)] = arg split " " match {
    case Array(_, Base64(Credential(username, password))) => Some(username, password)
    case _ => None
  }
}
