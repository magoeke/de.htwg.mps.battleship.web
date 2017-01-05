package controllers

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill}
import de.htwg.mps.battleship.Battleship
import de.htwg.mps.battleship.controller._
import de.htwg.mps.battleship.controller.command.{Command, Nothing, QuitGame, SetShip}

import scala.collection.immutable.ListMap
import scala.collection.mutable.{ListBuffer, Map}


class GameActor(system:ActorSystem) extends Actor {

  val game = ControllerFactory.create(system, Battleship.setUp())
  val players = ListBuffer[ActorRef]()
  val playerNameMapping = Map[String, String]()
  val playerToShips = Map[String, List[SetShip]]()
  var currentPlayer: String = _

  game ! RegisterUI

  override def receive = {
    case JoinSpecificGame(name, ships, actor) => join(name, ships, actor); game ! Nothing // send Nothing to get game infos
    case CommandProxy(player, command) => if(player == currentPlayer) game ! command
    case update: UpdateUI => currentPlayer = mapName(update.currentPlayer); broadcast(modify(update))
    case LeaveGame(player) => handleQuit(sender(), player)
    case Winner(player) => broadcast(Winner(mapName(player))); quit();
  }

  private def broadcast(any: Any) = players.foreach(_ ! any)

  private def modify(update: UpdateUI) = {
    UpdateUI(mapName(update.currentPlayer), mapGameInformation(update.gameInformation))
  }

  private def mapGameInformation(infos: List[GameInformation]) = {
    infos.map(info => info.copy(player=mapName(info.player)))
  }

  private def mapName(name: String) = playerNameMapping.getOrElse(name, name)

  private def join(name: String, ships: List[SetShip], actor: ActorRef) = {
    players += actor
    playerNameMapping += ("player"+playerNameMapping.size -> name)
    playerToShips += (name -> ships)
    if(playerNameMapping.size == 2) { startGame() }
  }

  private def startGame() = {
    for((k, v) <- ListMap(playerNameMapping.toSeq.sortBy(_._1):_*)) { playerToShips(v).foreach(game ! _) }
    playerToShips.clear()
    broadcast(StartGame)
  }

  private def handleQuit(actorRef: ActorRef, player: String) = {
    players -= actorRef
    playerNameMapping.map(_.swap) -= player
    if(players.length == 1) {
      players.head ! Winner(playerNameMapping.head._2)
      quit()
    }
  }

  private def quit() = {
    game ! PoisonPill
    self ! PoisonPill
  }
}

case object StartGame
case class LeaveGame(player: String)
case class CommandProxy(player: String, command: Command)
