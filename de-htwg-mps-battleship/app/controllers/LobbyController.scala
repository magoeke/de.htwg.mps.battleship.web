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

class LobbyController @Inject() (implicit system: ActorSystem, materializer: Materializer, lobbyActor: ILobbyActor) extends Controller {

  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => LobbySocketActor.props(out, request.session))
  }

  object LobbySocketActor {
    def props(out: ActorRef, session: Session) = Props(new LobbySocketActor(out, session))
  }

  class LobbySocketActor(out: ActorRef, session: Session) extends Actor {
    val lobby = UUID.fromString(session.get("lobby-id").get)
    val user = UUID.fromString(session.get("user").get)

    lobbyActor.actorRef ! RegisterLobbyActor(lobby)

    override def receive = {
      case msg: String => lobbyActor.actorRef ! BroadcastMessage(lobby, user, msg)
      case SendMessage(msg) => out ! msg
      case UpdatePlayers(msg) => out ! msg
    }

    override def postStop() = lobbyActor.actorRef ! DeregisterLobbyActor(lobby)
  }
}
