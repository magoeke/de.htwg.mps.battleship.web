package controllers

import javax.inject.Inject

import akka.actor.{Actor, ActorSystem}
import play.api.mvc._
import de.htwg.mps.battleship.Battleship

/**
  * Created by max on 01.01.17.
  */
class GameController @Inject() (system: ActorSystem) extends Controller {

  var defaultSettings = Battleship.setUp()

  def setShip(start: Int, end: Int) = Action { request =>
    Ok("")
  }

  def getBoardSize = Action { request =>
    Ok(defaultSettings(0).board.field.length.toString())
  }

  def getNumberOfPlayers = Action { request =>
    Ok(defaultSettings.length.toString())
  }

  def getShips = Action { request =>
    Ok(defaultSettings(0).board.ships.map(_.size).toString().replace("List", ""))
  }
}

class UiActor extends Actor {
  override def receive: Receive = {
    case _ => println("message arrived...")
  }
}
