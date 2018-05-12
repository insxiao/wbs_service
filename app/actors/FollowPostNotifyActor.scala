package actors

import actors.FollowPostNotifyActor.Follow
import akka.actor.{Actor, ActorRef, Props}

class FollowPostNotifyActor(out: ActorRef) extends Actor {
  override def receive: Receive = {
    case Follow(_) =>
  }
}

object FollowPostNotifyActor {
  case class Follow(userId: Long)
  def props(out: ActorRef) = Props(new FollowPostNotifyActor(out))
}