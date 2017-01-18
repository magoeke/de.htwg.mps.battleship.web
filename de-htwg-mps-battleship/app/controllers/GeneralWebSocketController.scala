package controllers

import java.util.UUID
import javax.inject.Inject

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.stream.Materializer
import de.htwg.mps.battleship.Point
import de.htwg.mps.battleship.controller.{UpdateUI, Winner}
import de.htwg.mps.battleship.controller.command.Fire
import play.api.mvc._
import play.api.libs.json.{JsObject, _}
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

  object WebSocketActor {
    def props(out: ActorRef) = Props(new WebSocketActor(out))
  }

  class WebSocketActor(out: ActorRef) extends Actor {

    val playerName = UUID.randomUUID().toString
    val setShip = system.actorOf(Props(new SetShipActor(playerName)))
    var gameActorRef: ActorRef = _

    setShip ! StartShipSetting

    override def receive = {
      case msg: String => handleWebSocketCommands(msg)
      case infos: UpdateUI => out ! answer(infos)
      case InitialGame(ships) => gameBroker.actorRef ! JoinGame(playerName, ships); out ! waitForSecondPlayer
      case Game(actorRef) => gameActorRef = actorRef;
      case StartGame => out ! startGame
      case Winner(player) => out ! winner(player); println(player)
    }

    private def answer(infos: UpdateUI) = {
      val gameInformation = infos.gameInformation.filter(info => info.player == playerName).head

      Json.stringify(JsObject(Seq(
        "type" -> JsString("update"),
        "currentPlayer" -> JsBoolean(gameInformation.player == playerName),
        "ships" -> Json.toJson(gameInformation.setableShips),
        "board" -> Json.toJson(gameInformation.boards.map(_.flatMap(row => row.map(_.toString()))))
      )))
    }


    private def winner(player: String) = {
      Json.stringify(JsObject(Seq(
        "type" -> JsString("winner"),
        "won" -> JsBoolean(player == playerName)
      )))
    }

    private def handleWebSocketCommands(msg: String): Unit = {
      val json = Try(Json.parse(msg)).getOrElse(null)
      (json \ "type").as[String] match {
        case "setShip" => setShip ! SendShip(calculatePoint((json \ "start").as[String]), calculatePoint((json \ "end").as[String]))
        case "fire" => gameActorRef ! CommandProxy(playerName, Fire(calculatePoint((json \ "index").as[String])))
        case _ => ;
      }
    }

    private def waitForSecondPlayer = Json.stringify(JsObject(Seq("type" -> JsString("waitForSecondPlayer"))))
    private def startGame = Json.stringify(JsObject(Seq("type" -> JsString("playersJoined"))))

    private def calculatePoint(index: String) : Point = calculatePoint(index.toInt)
    private def calculatePoint(index: Int) : Point = calculatePoint(index, 10)
    private def calculatePoint(index:Int, boardSize: Int) = {
      val x = index % boardSize
      Point((index - x) / boardSize, x )
    }
  }

}
