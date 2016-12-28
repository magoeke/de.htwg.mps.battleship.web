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

class GameBrowserController @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller {

  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => GameBrowserSocketActor.props(out))
  }

  object GameBrowserSocketActor {
    def props(out: ActorRef) = Props(new GameBrowserSocketActor(out))
  }

  class GameBrowserSocketActor(out: ActorRef) extends Actor {

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
      case msg: String => msg match {
        case "" => out ! Json.toJson(WebState.games).toString()
        case _ => addGame(msg); out ! Json.toJson(WebState.games).toString()
      }
    }

    private def addGame(str: String) = {
      val json: JsValue = Json.parse(str)
      val name = (json \ "name").asOpt[String].getOrElse("default")
      val maxPlayers = (json \ "player").asOpt[Int].getOrElse(2)
      WebState.addGame(name, maxPlayers)
//      games += GameBrowserEntry(UUID.randomUUID(), name, 0, maxPlayers)
    }
  }
}

