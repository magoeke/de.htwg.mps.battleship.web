package controllers

import akka.actor.{Actor, ActorRef, ActorSystem}
import de.htwg.mps.battleship.Battleship
import de.htwg.mps.battleship.controller.{ControllerFactory, GameInformation, RegisterUI, UpdateUI}
import de.htwg.mps.battleship.controller.command.{Command, Nothing, SetShip}

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
    println(playerNameMapping)
    playerToShips += (name -> ships)
    if(playerNameMapping.size == 2) { startGame() }
  }

  private def startGame() = {
    for((k, v) <- ListMap(playerNameMapping.toSeq.sortBy(_._1):_*)) { playerToShips(v).foreach(game ! _) }
    playerToShips.clear()
    broadcast(StartGame)
  }
}

case object StartGame
case class CommandProxy(player: String, command: Command)
