package controllers

import javax.inject.Inject

import akka.actor.{Actor, ActorSystem}
import play.api.mvc._
import de.htwg.mps.battleship.Battleship

/**
  * Created by max on 01.01.17.
  */
class GameController @Inject() (system: ActorSystem) extends Controller {

  var default_settings = Battleship.setUp()

  def setShip(start: Int, end: Int) = Action { request =>
    Ok("")
  }

  def getBoardSize = Action { request =>
    Ok("")
  }
}

class UiActor extends Actor {
  override def receive: Receive = {
    case _ => println("message arrived...")
  }
}
