package controllers

import javax.inject.{Inject}

import play.api._
import play.api.mvc._
import play.api.libs.streams._

import akka.actor._
import akka.stream.Materializer

class Application @Inject() (implicit system: ActorSystem,
                             materializer: Materializer,
                             webJarAssets: WebJarAssets) extends Controller {

  def index = Action {
    Ok(views.html.Application.index(webJarAssets))
  }

  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => MyWebSocketActor.props(out))
  }
}

object MyWebSocketActor {
  def props(out: ActorRef) = Props(new MyWebSocketActor(out))
}

class MyWebSocketActor(out: ActorRef) extends Actor {
  override def receive = {
    case msg: String => out ! ("I reeived yout message: " + msg)
  }
}