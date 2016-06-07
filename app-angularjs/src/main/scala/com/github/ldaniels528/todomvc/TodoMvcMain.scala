package com.github.ldaniels528.todomvc

import com.github.ldaniels528.meansjs.angularjs.{Scope, angular}
import com.github.ldaniels528.meansjs.core.browser.console
import com.github.ldaniels528.todomvc.controllers.TodoController
import com.github.ldaniels528.todomvc.services._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * Todo MVC application (MEANS.js demo)
  * @author lawrence.daniels@gmail.com
  */
@JSExportAll
object TodoMvcMain extends js.JSApp {

  override def main(): Unit = {
    // create the application
    val module = angular.createModule("todomvc", js.Array("ngRoute", "toaster"))

    // configure the Angular.js components
    module.controllerOf[TodoController]("TodoCtrl")
    module.serviceOf[TodoStorageService]("todoStorage")

    // initialize the application
    module.run({ ($rootScope: Scope) =>
      console.log("Initializing Todo MVC...")
    })
    ()
  }

}
