import scala.util.{Try, Failure, Success, Either, Left, Right}
object Extensions {

  implicit class RichEither[A <: Throwable, B](e: Either[A, B]) {
    def toTry: Try[B] = e.fold(Failure(_), Success(_))
  }

  implicit class RichTry[T](t: Try[T]) {
    def toEither: Either[_, T] = t.fold(Left(_), Right(_))
  }
}
