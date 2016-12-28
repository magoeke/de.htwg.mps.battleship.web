package controllers

import java.util.UUID
import javax.inject.Inject

import scala.collection.mutable.ListBuffer

import play.api._
import play.api.mvc._
import play.api.libs.streams._
import play.api.libs.json._

import akka.actor._
import akka.stream.Materializer

class LobbyController @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller {

    def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => LobbySocketActor.props(out))
  }

  object LobbySocketActor {
    def props(out: ActorRef) = Props(new LobbySocketActor(out))
  }

  class LobbySocketActor(out: ActorRef) extends Actor {

    override def receive = {
      case msg: String => out ! Json.toJson(msg).toString()
    }
  }
}
