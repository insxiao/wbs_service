package controllers

import actors.NewPostNotifyActor
import akka.actor._
import akka.stream._
import akka.stream.scaladsl.{Flow, Source}
import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.streams.ActorFlow

@Singleton
class NotifyController @Inject()(val cc: ControllerComponents)
                                (implicit val actorSystem: ActorSystem,
                                 val materializer: Materializer)
  extends AbstractController(cc) {

  def accept: WebSocket = WebSocket.accept[JsValue, JsValue] {
    implicit request =>
      ActorFlow.actorRef { out: ActorRef =>
        Props(new NewPostNotifyActor(out))
      }
  }
}
