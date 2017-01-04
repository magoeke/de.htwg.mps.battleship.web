package controllers

import akka.actor.{Actor, ActorRef, ActorSystem}
import de.htwg.mps.battleship.Battleship
import de.htwg.mps.battleship.controller.{ControllerFactory, RegisterUI, UpdateUI}
import de.htwg.mps.battleship.controller.command.{Command, Nothing}

import scala.collection.mutable.ListBuffer


class GameActor(system:ActorSystem) extends Actor {

  val game = ControllerFactory.create(system, Battleship.setUp() union List())
  val players = ListBuffer[ActorRef]()

  game ! RegisterUI

  override def receive = {
    case JoinGame(actor) => players += actor; game ! Nothing // send Nothing to get game infos
    case command: Command => game ! command
    case update: UpdateUI => broadcast(update)
  }

  private def broadcast(update: UpdateUI) = players.foreach(_ ! update)
}
