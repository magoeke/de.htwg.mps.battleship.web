package controllers

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import akka.actor._
import play.api.libs.json._

import com.google.inject.ImplementedBy

@ImplementedBy(classOf[LobbyActor])
trait ILobbyActor {
  def actorRef: ActorRef
}

@Singleton
class LobbyActor @Inject() (implicit system: ActorSystem) extends ILobbyActor{
  val lobbyActor = system.actorOf(Props[LobbyActorImpl])
  override def actorRef = lobbyActor
}

class LobbyActorImpl extends Actor {

  private val connections = Map[UUID, List[ActorRef]]()

  override def receive = {
    case RegisterLobbyActor(lobby) => register(lobby, sender());
    case DeregisterLobbyActor(lobby) => deregister(lobby, sender())
    case BroadcastMessage(lobby, sender, message) => sendMessage(lobby, sender, message)
  }

  private def broadcast(lobby: UUID, msg: Message) = connections(lobby).foreach(unicast(_, msg))
  private def unicast(actor: ActorRef, msg: Message) = actor ! msg

  private def register(lobbyID: UUID, actor: ActorRef) = {
    if(connections.exists(_._1 == lobbyID)) {
      connections(lobbyID) = connections(lobbyID) union List(actor)
    } else {
      connections += (lobbyID -> List(actor))
    }
  }

  private def deregister(lobbyID: UUID, actor: ActorRef) = {
    if(connections.exists(_._1 == lobbyID)) {
      connections(lobbyID) = connections(lobbyID) diff List(actor)
      if(connections(lobbyID).isEmpty) {
        connections -= lobbyID
      }
    }
  }

  private def sendMessage(lobby: UUID, sender: UUID, msg: String) = {
    val json: JsObject = Json.parse(msg).as[JsObject]
    val message: JsObject = JsObject(Seq("sender" -> JsString(sender.toString())))

    broadcast(lobby, SendMessage(Json.stringify(json ++ message)))
  }
}

