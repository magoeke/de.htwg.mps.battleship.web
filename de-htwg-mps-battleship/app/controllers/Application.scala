package controllers

import java.util.UUID
import javax.inject.Inject

import play.api._
import play.api.mvc._
import play.api.mvc.Result._

class Application @Inject() (webJarAssets: WebJarAssets) extends Controller {

  def index = Action { request =>
    returnGame(request.session)
  }

  private def returnGame(session: Session) = {
    val page = Ok("not created yet")
    SessionHandler.handle(session, "game-id",
      (s: Session) => page.withSession(s),
      (s: Session) => returnLobby(s))
  }

  private def returnLobby(session: Session) = {
    val page = Ok(views.html.Application.lobby(webJarAssets))
    SessionHandler.handle(session, "lobby-id",
      (s: Session) => page.withSession(s),
      (s: Session) => returnGameBrowser(s))
  }

  private def returnGameBrowser(session: Session) : Result = {
    val page = Ok(views.html.Application.index(webJarAssets))
    SessionHandler.handle(session, "user",
      (s: Session) => page.withSession(s),
      (s: Session) => page.withSession(s + ("user" -> UUID.randomUUID().toString())))
  }

  def setLobby(id: String) = Action { request =>
    Ok("").withSession(request.session + ("lobby-id" -> id))
  }

  def unsetLobby = Action { request =>
    Ok("").withSession(request.session - "lobby-id")
  }

}

object SessionHandler {
  def handle(session: Session, property: String, func1: (Session) => Result, func2: (Session) => Result): Result = {
    session.get(property) match {
      case Some(_) => func1(session)
      case None => func2(session)
    }
  }
}
