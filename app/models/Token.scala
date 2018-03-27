package models


case class Token(id: Long, username: String) {

  import util.encrypt.CredentialDigest
  import extensions.Ex.Base64Convert

  def toTokenString: String = s"$id:$username:$sha1".encode

  def sha1: String = {
    val up = s"$id:$username"
    CredentialDigest.digest(up)
  }
}
