package controllers

import java.util.UUID
import javax.inject.Inject

import scala.collection.mutable.ListBuffer

import play.api._
import play.api.mvc._
import play.api.libs.streams._
import play.api.libs.json._

import akka.actor._
import akka.stream.Materializer

class LobbyController @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller {

  val games = new ListBuffer[LobbyEntry]()

  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => LobbySocketActor.props(out))
  }

  object LobbySocketActor {
    def props(out: ActorRef) = Props(new LobbySocketActor(out))
  }

  class LobbySocketActor(out: ActorRef) extends Actor {
    implicit val EntryWrites = new Writes[LobbyEntry] {
      def writes(entry: LobbyEntry) = Json.obj(
        "UUID" -> entry.id,
        "name" -> entry.name,
        "player" -> entry.player,
        "maxPlayers" -> entry.maxPlayers
      )
    }

    override def receive = {
      case msg: String => addGame(msg); out ! Json.toJson(games).toString()
    }

    private def addGame(str: String) = {
      val json: JsValue = Json.parse(str)
      val name = (json \ "name").asOpt[String].getOrElse("default")
      val maxPlayers = (json \ "player").asOpt[Int].getOrElse(2)
      games += LobbyEntry(UUID.randomUUID(), name, 0, maxPlayers)
    }
  }
}

case class LobbyEntry(id: UUID, name: String, player: Int, maxPlayers: Int) {}