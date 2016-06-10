package com.github.ldaniels528.todomvc.controllers

import java.lang.{Boolean => JBoolean}

import com.github.ldaniels528.meansjs.angularjs.AngularJsHelper._
import com.github.ldaniels528.meansjs.angularjs._
import com.github.ldaniels528.meansjs.angularjs.toaster.Toaster
import com.github.ldaniels528.meansjs.util.ScalaJsHelper._
import com.github.ldaniels528.todomvc.controllers.TodoController._
import com.github.ldaniels528.todomvc.models.Todo
import com.github.ldaniels528.todomvc.services.TodoStorageService

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * TodoMVC Controller
  * @author lawrence.daniels@gmail.com
  */
class TodoController($scope: TodoScope, $routeParams: TodoRouteParams, $filter: Filter, toaster: Toaster,
                     @injected("todoStorage") todoStorage: TodoStorageService) extends Controller {

  // initialize the scope variables
  $scope.todos = emptyArray
  $scope.newTodo = ""
  $scope.editedTodo = null
  $scope.reverted = null
  $scope.status = $routeParams.status getOrElse ""
  $scope.statusFilter = StatusFilter.forStatus($scope.status)

  // retrieve the todos
  loadTodos()

  /////////////////////////////////////////////////////////////////////////////////
  //      Public Methods
  /////////////////////////////////////////////////////////////////////////////////

  $scope.addTodo = () => {
    val newTodo = Todo(title = $scope.newTodo.trim, completed = false)
    if (newTodo.hasTitle) {
      $scope.saving = true

      todoStorage.create(newTodo) onComplete {
        case Success(todos) =>
          $scope.saving = false
          $scope.newTodo = ""
          $scope.editedTodo = null
          $scope.reverted = null
          $scope.$apply(() => setTodos(todos))
        case Failure(e) =>
          $scope.saving = false
          toaster.error("Add Error", e.displayMessage)
      }
    }
  }

  $scope.clearCompletedTodos = () => {
    todoStorage.deleteCompleted() onComplete {
      case Success(todos) => $scope.$apply(() => setTodos(todos))
      case Failure(e) =>
        toaster.error("Update Error", e.displayMessage)
    }
  }

  $scope.editTodo = (todo: Todo) => {
    $scope.editedTodo = todo
    $scope.originalTodo = todo.copy()
  }

  $scope.markAll = (completed: Boolean) => {
    val updatableTodos = $scope.todos.filterNot(_.completed == completed) map { todo => todo.completed = completed; todo }
    todoStorage.update(updatableTodos) onComplete {
      case Success(todos) => $scope.$apply(() => setTodos(todos))
      case Failure(e) =>
        toaster.error("Update Error", e.displayMessage)
    }
  }

  $scope.removeTodo = (todo: Todo) => {
    todoStorage.delete(todo) onComplete {
      case Success(todos) =>
        $scope.$apply(() => setTodos(todos))
      case Failure(e) =>
        toaster.error("Remove Error", e.displayMessage)
    }
  }

  $scope.revertEdits = (todo: Todo) => {
    $scope.todos($scope.todos.indexOf(todo)) = $scope.originalTodo
    $scope.editedTodo = null
    $scope.originalTodo = null
    $scope.reverted = true
  }

  $scope.saveEdits = (todo: Todo, event: String) => {
    // Blur events are automatically triggered after the form submit event.
    // This does some unfortunate logic handling to prevent saving twice.
    if (event == "blur" && $scope.saveEvent == "submit") $scope.saveEvent = null
    else {
      $scope.saveEvent = event
      if ($scope.reverted) {
        // Todo edits were reverted-- don"t save.
        $scope.reverted = null
      }
      else {
        todo.title = todo.title.trim()
        if (todo.title == $scope.originalTodo.title) $scope.editedTodo = null
        else {
          val task = if (todo.title.trim().nonEmpty) todoStorage.update(todo) else todoStorage.delete(todo)
          task onComplete {
            case Success(_) =>
              $scope.editedTodo = null
            case Failure(e) =>
              todo.title = $scope.originalTodo.title
              $scope.editedTodo = null
          }
        }
      }
    }
  }

  $scope.saveTodo = (todo: Todo) => {
    todoStorage.update(todo) onComplete {
      case Success(todos) =>
        $scope.$apply(() => setTodos(todos))
      case Failure(e) =>
        toaster.error("Update Error", e.displayMessage)
    }
  }

  /////////////////////////////////////////////////////////////////////////////////
  //      Event Handlers
  /////////////////////////////////////////////////////////////////////////////////

  // Monitor the current route for changes and adjust the filter accordingly.
  $scope.$on("$routeChangeSuccess", () => {
    $scope.status = $routeParams.status getOrElse ""
    $scope.statusFilter = $scope.status match {
      case "active" => StatusFilter(completed = false)
      case "completed" => StatusFilter(completed = true)
      case _ => StatusFilter()
    }
  })

  /////////////////////////////////////////////////////////////////////////////////
  //      Private Methods
  /////////////////////////////////////////////////////////////////////////////////

  private def loadTodos() = {
    todoStorage.getAll onComplete {
      case Success(todos) =>
        $scope.$apply(() => setTodos(todos))
      case Failure(e) =>
        toaster.error("Retrieve Error", e.displayMessage)
    }
  }

  private def setTodos(todos: js.Array[Todo]): Unit = {
    $scope.todos = todos
    $scope.remainingCount = $scope.todos.count(!_.completed)
    $scope.completedCount = $scope.todos.length - $scope.remainingCount
    $scope.allChecked = $scope.remainingCount == 0
  }

}

/**
  * TodoMVC Controller Companion
  * @author lawrence.daniels@gmail.com
  */
object TodoController {

  /**
    * TodoMVC Scope
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait TodoScope extends Scope {
    var todos: js.Array[Todo] = js.native
    var editedTodo: Todo = js.native
    var originalTodo: Todo = js.native
    var newTodo: String = js.native

    var allChecked: Boolean = js.native
    var completedCount: Int = js.native
    var remainingCount: Int = js.native
    var saveEvent: String = js.native
    var saving: Boolean = js.native
    var status: String = js.native
    var reverted: JBoolean = js.native
    var statusFilter: StatusFilter = js.native

    // functions
    var addTodo: js.Function0[Unit] = js.native
    var clearCompletedTodos: js.Function0[Unit] = js.native
    var editTodo: js.Function1[Todo, Unit] = js.native
    var markAll: js.Function1[Boolean, Unit] = js.native
    var removeTodo: js.Function1[Todo, Unit] = js.native
    var revertEdits: js.Function1[Todo, Unit] = js.native
    var saveEdits: js.Function2[Todo, String, Unit] = js.native
    var saveTodo: js.Function1[Todo, Unit] = js.native

  }

  /**
    * TodoMVC Route Params
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait TodoRouteParams extends js.Object {
    var status: js.UndefOr[String] = js.native
  }

  /**
    * Status Filter
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait StatusFilter extends js.Object {
    var completed: js.UndefOr[Boolean] = js.native
  }

  /**
    * Status Filter Companion
    * @author lawrence.daniels@gmail.com
    */
  object StatusFilter {

    def forStatus(status: String) = {
      status match {
        case "active" => StatusFilter(completed = false)
        case "completed" => StatusFilter(completed = true)
        case _ => StatusFilter()
      }
    }

    def apply(completed: js.UndefOr[Boolean] = js.undefined) = {
      val filter = New[StatusFilter]
      filter.completed = completed
      filter
    }

  }

}
