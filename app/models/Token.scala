package models

case class Token(id: Long, username: String) {

  import extensions.Ex.{Sha1Hash, Base64Convert}

  def toTokenString: String = s"$id:$username:$sha1".encode

  def sha1: String = s"$id:$username".sha1
}
