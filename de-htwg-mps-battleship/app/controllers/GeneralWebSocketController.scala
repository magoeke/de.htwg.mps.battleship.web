package controllers

import javax.inject.Inject

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import de.htwg.mps.battleship.controller.{UpdateUI}
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.streams.ActorFlow

import scala.util.Try

/**
  * Created by max on 01.01.17.
  */
class GeneralWebSocketController @Inject() (implicit system: ActorSystem,
                                            materializer: Materializer,
                                            gameBroker: IGameBroker) extends Controller {

  def initWebSocket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => WebSocketActor.props(out))
  }

  /*private def getBoardSize = defaultSettings(0).board.field.length
  private def getShips = defaultSettings(0).board.ships*/

  object WebSocketActor {
    def props(out: ActorRef) = Props(new WebSocketActor(out))
  }

  class WebSocketActor(out: ActorRef) extends Actor {

    val setShip = system.actorOf(Props[SetShipActor])
    val playerName = "player0"

    setShip ! StartShipSetting

    override def receive = {
      case msg: String => handleWebSocketCommands(msg)
      case infos: UpdateUI => out ! answer(infos)
    }

    private def answer(infos: UpdateUI) = {
      val gameInformation = infos.gameInformation.filter(info => info.player == playerName).head

      Json.stringify(JsObject(Seq(
        "type" -> JsString("update"),
        "ships" -> Json.toJson(gameInformation.setableShips),
        "board" -> Json.toJson(gameInformation.boards(0).flatMap(row => row.map(_.toString())))
      )))
    }

    private def handleWebSocketCommands(msg: String): Unit = {
      val json = Try(Json.parse(msg)).getOrElse(null)
      (json \ "type").as[String] match {
        case "setShip" => setShip ! SendShip((json \ "start").as[String], (json \ "end").as[String])
        case _ => ;
      }
    }
  }

}
