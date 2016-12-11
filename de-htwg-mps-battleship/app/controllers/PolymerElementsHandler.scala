package controllers

import java.io.File
import play.api.mvc.{Action, Controller}

class PolymerElementHandler extends Controller {
    val basePath = System.getenv("PWD") + "/target/web/public/main/lib/github-com-PolymerElements-"
    val secondPath = System.getenv("PWD") + "/target/web/public/main/lib/github-com-polymerelements-"
    
    def redirectToElement(polymerPath: String) = Action {
	//println(System.getenv("PWD"))
	//println(polymerPath)
         try {
              val fileStream: java.io.InputStream = new java.io.FileInputStream(basePath + polymerPath)
              val fileString = scala.io.Source.fromInputStream(fileStream).mkString("")
              Ok(fileString) as HTML
         } catch {
             case _ : Throwable => {try {
                 val stream: java.io.InputStream = new java.io.FileInputStream(secondPath + polymerPath)
                 val fileString = scala.io.Source.fromInputStream(stream).mkString("")
                 Ok(fileString) as HTML
             } catch {
                 case _ : Throwable => NotFound(polymerPath)
             }}
         }
         //println(polymerPath)
         //Ok(polymerPath)
         //Ok(routes.Assets.at("lib/github-com-PolymerElements-"+polymerPath))
         //Redirect(routes.Assets.at("lib/github-com-PolymerElements-"+polymerPath))
    }

    def getPolymer(path: String) = Action {
	println(path)
        Redirect(routes.Assets.at("lib/polymer/"+path))
    }
}
