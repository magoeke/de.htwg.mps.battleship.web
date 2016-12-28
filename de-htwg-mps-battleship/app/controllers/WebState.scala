package controllers

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import akka.actor._
import play.api.libs.concurrent.Akka

import com.google.inject.ImplementedBy

@ImplementedBy(classOf[GameList])
trait IGameList {
  def actorRef: ActorRef
}

@Singleton
class GameList @Inject() (implicit system: ActorSystem) extends IGameList{
  val webStateActor = system.actorOf(Props[WebStateActor])
  override def actorRef = webStateActor
}


class WebStateActor extends Actor {

  private val gamesMap = Map[UUID, GameEntry]()
  private val connections = ListBuffer[ActorRef]()

  override def receive = {
    case RegisterActor => connections += sender()
    case DeregisterActor => connections -= sender()
    case JoinLobby(lobbyID, playerID) => joinLobby(lobbyID, playerID)
    case LeaveLobby(lobbyID, playerID) => leaveLobby(lobbyID, playerID)
    case AddGame(name, maxPlayers) => addGame(name, maxPlayers); connections.foreach(_ ! UpdateGameBrowser(gamesMap))
  }

  private def addGame(name: String, maxPlayers: Int) = {
    gamesMap += (UUID.randomUUID() -> GameEntry(name, List[UUID](), maxPlayers))
  }

  private def joinLobby(lobbyID: UUID, playerID: UUID) = {
    gamesMap(lobbyID) = gamesMap(lobbyID).copy(players = playerID :: gamesMap(lobbyID).players)
  }

  private def leaveLobby(lobbyID: UUID, playerID: UUID) = {
    gamesMap(lobbyID) = gamesMap(lobbyID).copy(players = gamesMap(lobbyID).players diff List(playerID))
  }

  private def games : Map[UUID, GameEntry] = gamesMap
}

case object RegisterActor
case object DeregisterActor
case class UpdateGameBrowser(games: Map[UUID, GameEntry])
case class JoinLobby(lobbyID: UUID, playerID:UUID)
case class LeaveLobby(lobbyID: UUID, playerID:UUID)
case class AddGame(name: String, maxPlayers: Int)

case class GameEntry(name: String, players: List[UUID], maxPlayers: Int) {
  def taken = players.length
}