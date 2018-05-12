package actors

import akka.actor._

import scala.collection.mutable

class ActorRegistry extends Actor {
  private val actors = collection.mutable.TreeSet.empty[ActorRef]
  override def receive: Receive = {
    case actorRef: ActorRef =>
      actors += actorRef
  }
}
