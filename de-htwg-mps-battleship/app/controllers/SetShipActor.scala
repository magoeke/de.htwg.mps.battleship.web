package controllers

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.actor.Actor.Receive
import de.htwg.mps.battleship.{Battleship, Point}
import de.htwg.mps.battleship.controller.{BattleshipController, ControllerFactory, RegisterUI, UpdateUI}
import de.htwg.mps.battleship.controller.command.{Command, SetShip}

import scala.collection.mutable.{ListBuffer, Map}

/**
  * Created by max on 04.01.17.
  */
class SetShipActor(system: ActorSystem) extends Actor{

  val defaultSettings = Battleship.setUp()
  val tmpGame = new BattleshipController(defaultSettings)
  //val tmpGame = ControllerFactory.create(system, defaultSettings)
  val shipCommands = ListBuffer[SetShip]()
  val playerName = defaultSettings.head.name

  //tmpGame ! RegisterUI

  override def receive: Receive = {
    case SendShip(start, end) => createSetShip(sender(), start, end)
    case update: UpdateUI =>
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