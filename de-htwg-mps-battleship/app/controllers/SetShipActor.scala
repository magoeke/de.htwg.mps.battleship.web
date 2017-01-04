package controllers

import akka.actor.{Actor, ActorRef}
import de.htwg.mps.battleship.{Battleship, Point}
import de.htwg.mps.battleship.controller.{BattleshipController, GameInformation, UpdateUI}
import de.htwg.mps.battleship.controller.command.{Command, SetShip}

import scala.collection.mutable.ListBuffer

/**
  * Created by max on 04.01.17.
  */
class SetShipActor(playerName: String) extends Actor{

  val defaultSettings = Battleship.setUp()
  val tmpGame = new BattleshipController(defaultSettings)
  val shipCommands = ListBuffer[SetShip]()
  val firstPlayer = defaultSettings.head.name

  override def receive: Receive = {
    case SendShip(start, end) => createSetShip(sender(), start, end)
    case StartShipSetting => sender() ! createUpdateUI
  }

  private def modifyPlayer(infos: List[GameInformation]) = {
    infos.map(info=> if(info.player == firstPlayer) { info.copy(player=playerName) } else { info })
  }

  private def createSetShip(sender: ActorRef, start: Point, end: Point) = {
    val command = SetShip(start, end)
    val tmpSetableShips = getSetableShips
    tmpGame.handleCommand(command)
    if(tmpSetableShips != getSetableShips) {
      shipCommands += command
      sender ! createUpdateUI
    }

    if(getSetableShips.isEmpty) { sender ! InitialGame(shipCommands.toList) }
  }

  private def createUpdateUI = UpdateUI(tmpGame.currentPlayer.name, modifyPlayer(tmpGame.collectGameInformation))
  private def getSetableShips = tmpGame.collectGameInformation.filter(info=> info.player == firstPlayer).head.setableShips

}
// Datastructure
case class CommandValid(command: Command, ships: List[Int])

// Message
case class SendShip(start: Point, end: Point)
case class InitialGame(ships: List[SetShip])
case object StartShipSetting