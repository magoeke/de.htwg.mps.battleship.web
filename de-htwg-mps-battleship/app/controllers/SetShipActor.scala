package controllers

import akka.actor.{Actor, ActorRef}
import de.htwg.mps.battleship.{Battleship, Point}
import de.htwg.mps.battleship.controller.{BattleshipController, UpdateUI}
import de.htwg.mps.battleship.controller.command.{Command, SetShip}

import scala.collection.mutable.ListBuffer

/**
  * Created by max on 04.01.17.
  */
class SetShipActor() extends Actor{

  val defaultSettings = Battleship.setUp()
  val tmpGame = new BattleshipController(defaultSettings)
  val shipCommands = ListBuffer[SetShip]()
  val playerName = defaultSettings.head.name

  override def receive: Receive = {
    case SendShip(start, end) => createSetShip(sender(), start, end)
    case StartShipSetting => sender() ! UpdateUI(tmpGame.currentPlayer.name, tmpGame.collectGameInformation)
  }

  private def createSetShip(sender: ActorRef, start: String, end: String) = {
    val command = SetShip(calculatePoint(start.toInt), calculatePoint(end.toInt))
    val tmpSetableShips = getSetableShips
    tmpGame.handleCommand(command)
    if(tmpSetableShips != getSetableShips) {
      shipCommands += command
      sender ! UpdateUI(tmpGame.currentPlayer.name, tmpGame.collectGameInformation)
    }

    if(getSetableShips.isEmpty) { sender ! StartGame }
  }

  private def calculatePoint(index: Int) : Point = calculatePoint(index, defaultSettings.head.board.field.length)
  private def calculatePoint(index:Int, boardSize: Int) = {
    val x = index % boardSize
    Point((index - x) / boardSize, x )
  }

  private def getSetableShips = tmpGame.collectGameInformation.filter(info=> info.player == playerName).head.setableShips

}
// Datastructure
case class CommandValid(command: Command, ships: List[Int])

// Message
case class SendShip(start: String, end: String)
case object StartGame
case object StartShipSetting