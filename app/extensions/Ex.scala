package extensions

import scala.util.{Either, Failure, Left, Right, Success, Try}

object Ex {

  implicit class RichEither[A <: Throwable, B](e: Either[A, B]) {
    def toTry: Try[B] = e.fold(Failure(_), Success(_))
  }

  implicit class RichTry[T](t: Try[T]) {
    def toEither: Either[_, T] = t.fold(Left(_), Right(_))
  }

  implicit class Base64Convert(val s: String) {

    import java.util.Base64

    private def encoder = Base64.getEncoder

    private def decoder = Base64.getDecoder

    def encode: String = new String(encoder.encode(s.getBytes()))

    def decode: String = new String(decoder.decode(s))
  }

  implicit class Sha1Hash(val s: String) {
    private val N = "0123456789ABCDEF".toLowerCase.toCharArray
    def sha1: String = {
      val messageDigest = java.security.MessageDigest.getInstance("sha1")
      val hash = messageDigest.digest(s.getBytes())
      val hexBuilder = new StringBuilder
      for {
        byte <- hash
      } {
        val l = N(byte & 0x0f)
        val h = N((byte >> 4) & 0x0f)
        hexBuilder.append(h)
        hexBuilder.append(l)
      }
      hexBuilder.toString()
    }
  }
}
