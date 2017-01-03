package controllers

import javax.inject.Inject

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import de.htwg.mps.battleship.controller.{ControllerFactory, RegisterUI, UpdateUI}
import play.api.mvc._
import de.htwg.mps.battleship.{Battleship, Point}
//import de.htwg.mps.battleship.controller.command.SetShip
import de.htwg.mps.battleship.controller.command._
import play.api.libs.json._
import play.api.libs.streams.ActorFlow

import scala.collection.mutable.ListBuffer
import scala.util.Try

/**
  * Created by max on 01.01.17.
  */
class GameController @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller {
  val defaultSettings = Battleship.setUp()

  def initWebSocket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => GameSocketActor.props(out))
  }

  private def getBoardSize = defaultSettings(0).board.field.length
  private def getShips = defaultSettings(0).board.ships

  object GameSocketActor {
    def props(out: ActorRef) = Props(new GameSocketActor(out))
  }

  class GameSocketActor(out: ActorRef) extends Actor {
    val shipCommands = ListBuffer[SetShip]()
    val gameActor = ControllerFactory.create(system, defaultSettings union List())
    val playerName = defaultSettings(0).name

    // register
    gameActor ! RegisterUI

    override def receive = {
      case msg: String => handleWebSocketCommands(msg)
      case infos: UpdateUI => out ! answer(infos)
    }

    private def answer(infos: UpdateUI) = {
      val gameInformation = infos.gameInformation.filter(info => info.player == playerName).head
      Json.stringify(JsObject(Seq(
        "type" -> JsString("setShip"),
        "ships" -> Json.toJson(gameInformation.setableShips),
        "board" -> Json.toJson(gameInformation.boards(0).flatMap(row => row.map(_.toString())))
      )))
    }

    private def handleWebSocketCommands(msg: String): Unit = {
      val json = Try(Json.parse(msg)).getOrElse(null)
      println(json)
      (json \ "type").as[String] match {
        case "setShip" => saveShip((json \ "start").as[String], (json \ "end").as[String])
        case _ => ;
      }
    }

    private def saveShip(start: String, end: String) = {
      val command = SetShip(calculatePoint(start.toInt), calculatePoint(end.toInt))
      println(command)
      gameActor ! command
    }

    private def calculatePoint(index: Int) : Point = calculatePoint(index, getBoardSize)
    private def calculatePoint(index:Int, boardSize: Int) = {
      val x = index % boardSize
      Point((index - x) / boardSize, x )
    }
  }

}
