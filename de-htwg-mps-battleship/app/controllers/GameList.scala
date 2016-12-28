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
  val gameListActor = system.actorOf(Props[GameListActor])
  override def actorRef = gameListActor
}

class GameListActor extends Actor {

  private val gamesMap = Map[UUID, GameEntry]()
  private val connections = ListBuffer[ActorRef]()

  override def receive = {
    case RegisterActor => connections += sender(); unicast(sender())
    case DeregisterActor => connections -= sender()
    case JoinLobby(lobbyID, playerID) => joinLobby(lobbyID, playerID); broadcast()
    case LeaveLobby(lobbyID, playerID) => leaveLobby(lobbyID, playerID); broadcast()
    case AddGame(name, maxPlayers) => addGame(name, maxPlayers); broadcast()
  }

  private def broadcast() = connections.foreach(unicast(_))
  private def unicast(actor: ActorRef) = actor ! UpdateGameBrowser(gamesMap)
  private def addGame(name: String, maxPlayers: Int) = gamesMap += (UUID.randomUUID() -> GameEntry(name, List[UUID](), maxPlayers))
  private def joinLobby(lobbyID: UUID, playerID: UUID) = gamesMap(lobbyID) = gamesMap(lobbyID).copy(players = gamesMap(lobbyID).players union List(playerID))
  private def leaveLobby(lobbyID: UUID, playerID: UUID) = gamesMap(lobbyID) = gamesMap(lobbyID).copy(players = gamesMap(lobbyID).players diff List(playerID))
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