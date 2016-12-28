package controllers

import java.util.UUID
import javax.inject.Inject

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map

import play.api._
import play.api.mvc._
import play.api.libs.streams._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import akka.actor._
import akka.stream.Materializer

import models._

class GameBrowserController @Inject() (implicit system: ActorSystem, materializer: Materializer, gameList: IGameList) extends Controller {

//  val webStateActor = system.actorOf(WebState.props)

  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => GameBrowserSocketActor.props(out))
  }

  object GameBrowserSocketActor {
    private val connections = ListBuffer[ActorRef]()

    def props(out: ActorRef) = Props(new GameBrowserSocketActor(out))
  }

  class GameBrowserSocketActor(out: ActorRef) extends Actor {
    //register actor
    gameList.actorRef ! RegisterActor


    implicit val MapWrites = new Writes[Map[UUID, GameEntry]] {
      def writes(m: Map[UUID, GameEntry]) = {
        Json.toJson(m.map {
          case (key, value) => {
            Json.obj(
              "UUID" -> key,
              "name" -> value.name,
              "player" -> value.taken,
              "maxPlayers" -> value.maxPlayers
            )
          }
        })
      }
    }

    override def receive = {
      case msg: String => addGame(msg)
      case UpdateGameBrowser(games) => out ! Json.toJson(games).toString()
    }

    override def postStop() = gameList.actorRef ! DeregisterActor

    private def addGame(str: String) = {
      val json: JsValue = Json.parse(str)
      val name = (json \ "name").asOpt[String].getOrElse("default")
      val maxPlayers = (json \ "player").asOpt[Int].getOrElse(2)
      gameList.actorRef ! AddGame(name, maxPlayers)
    }
  }
}

