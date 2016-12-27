package controllers

import javax.inject.{Inject}

import play.api._
import play.api.mvc._

class Application @Inject() (webJarAssets: WebJarAssets) extends Controller {

  def index = Action {
    Ok(views.html.Application.index(webJarAssets))
  }

}
