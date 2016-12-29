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
class LobbyActor @Inject() (implicit system: ActorSystem, gameList: IGameList) extends ILobbyActor{
  val lobbyActor = system.actorOf(Props(new LobbyActorImpl(gameList.actorRef)))
  override def actorRef = lobbyActor
}

class LobbyActorImpl(val gameListActor: ActorRef) extends Actor {

  private val connections = Map[UUID, List[ActorRef]]()

  override def receive = {
    case RegisterLobbyActor(lobby) => register(lobby, sender()); gameListActor ! PlayerInLobby(lobby)
    case DeregisterLobbyActor(lobby) => deregister(lobby, sender()); gameListActor ! PlayerInLobby(lobby)
    case BroadcastMessage(lobby, sender, message) => sendMessage(lobby, sender, message)
    case BroadcastPlayerInLobby(lobby, players) => sendPlayers(lobby, players)
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

  private def sendPlayers(lobby: UUID, players: List[UUID]) = {
    val json: JsValue = JsObject(Seq(
      "type" -> JsString("players"),
      "players" -> Json.toJson(players)
    ))

    broadcast(lobby, UpdatePlayers(Json.stringify(json)))
  }
}

