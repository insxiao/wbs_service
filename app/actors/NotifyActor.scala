package actors
import akka.actor.{Actor, ActorRef, Props}

class NewPostNotifyActor(out: ActorRef) extends Actor {
  import NewPostNotifyActor._
  override def receive: Receive = {
    case Notify => out ! "haveNewPost"
  }
}

object NewPostNotifyActor {
  case object Notify
  def props(out: ActorRef) = Props(new NewPostNotifyActor(out))
}
