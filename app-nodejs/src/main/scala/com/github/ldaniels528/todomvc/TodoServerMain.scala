package com.github.ldaniels528.todomvc

import com.github.ldaniels528.meansjs.nodejs.bodyparser.{BodyParser, UrlEncodedBodyOptions}
import com.github.ldaniels528.meansjs.nodejs.express.{Express, Request, Response}
import com.github.ldaniels528.meansjs.nodejs.expressws._
import com.github.ldaniels528.meansjs.nodejs.global._
import com.github.ldaniels528.meansjs.nodejs.{Bootstrap, console}
import com.github.ldaniels528.todomvc.routes._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * Todo Server Main
  * @author lawrence.daniels@gmail.com
  */
@JSExportAll
object TodoServerMain extends js.JSApp {

  override def main(): Unit = {}

  def startServer(implicit bootstrap: Bootstrap) {
    implicit val require = bootstrap.require

    // determine the port to listen on
    val port = process.env.get("port").map(_.toInt) getOrElse 1337

    // setup Express
    console.log("Loading Express modules...")
    implicit val express = require[Express]("express")
    implicit val app = express().withWsRouting
    implicit val wss = require[ExpressWS]("express-ws")(app)

    // setup the body parsers
    console.log("Setting up body parsers...")
    val bodyParser = require[BodyParser]("body-parser")
    app.use(bodyParser.json())
    app.use(bodyParser.urlencoded(new UrlEncodedBodyOptions(extended = true)))

    // setup the routes for serving static files
    console.log("Setting up the routes for serving static files...")
    app.use(express.static("public"))
    app.use("/assets", express.static("public"))
    app.use("/bower_components", express.static("bower_components"))

    // disable caching
    app.disable("etag")

    // setup logging of the request - response cycles
    app.use((request: Request, response: Response, next: NextFunction) => {
      val startTime = System.currentTimeMillis()
      next()
      response.onFinish(() => {
        val elapsedTime = System.currentTimeMillis() - startTime
        console.log("[node] application - %s %s ~> %d [%d ms]", request.method, request.originalUrl, response.statusCode, elapsedTime)
      })
    })

    // setup searchable entity routes
    TodoRoutes.init(app)

    // start the listener
    app.listen(port, () => console.log("Server now listening on port %d", port))
    ()
  }

}
