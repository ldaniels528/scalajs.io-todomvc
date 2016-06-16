package com.github.ldaniels528.todomvc.routes

import java.util.UUID

import com.github.ldaniels528.todomvc.models.Todo
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Todo Routes
  * @author lawrence.daniels@gmail.com
  */
object TodoRoutes {
  private val todos: js.Array[Todo] = emptyArray

  def init(app: Application)(implicit ec: ExecutionContext, require: NodeRequire) = {
    // collections
    app.get("/api/todos", (request: Request, response: Response, next: NextFunction) => getTodos(request, response, next))
    app.delete("/api/todos/completed", (request: Request, response: Response, next: NextFunction) => deleteCompleted(request, response, next))
    app.put("/api/todos", (request: Request, response: Response, next: NextFunction) => updateTodos(request, response, next))

    // specific
    app.get("/api/todo/:id", (request: Request, response: Response, next: NextFunction) => getTodo(request, response, next))
    app.delete("/api/todo/:id", (request: Request, response: Response, next: NextFunction) => delete(request, response, next))
    app.post("/api/todo", (request: Request, response: Response, next: NextFunction) => createTodo(request, response, next))
    app.put("/api/todo", (request: Request, response: Response, next: NextFunction) => updateTodo(request, response, next))
  }

  def createTodo(request: Request, response: Response, next: NextFunction) = {
    request.bodyAs[Todo] match {
      case todo if todo.hasTitle =>
        todo.id = UUID.randomUUID().toString
        todos.push(todo)
        response.send(todos)
      case todo =>
        response.badRequest(todo)
    }
    next()
  }

  def deleteCompleted(request: Request, response: Response, next: NextFunction) = {
    todos.replaceWith(todos.filterNot(_.completed): _*)
    response.send(todos)
    next()
  }

  def delete(request: Request, response: Response, next: NextFunction) = {
    val todoId = request.params("id")
    todos.indexWhereOpt(_.id == todoId) match {
      case Some(index) =>
        todos.remove(index)
        response.send(todos)
      case None =>
        response.notFound(todoId)
    }
    next()
  }

  def getTodo(request: Request, response: Response, next: NextFunction) = {
    val todoId = request.params("id")
    todos.indexWhereOpt(_.id == todoId) match {
      case Some(index) => response.send(todos(index))
      case None => response.notFound(todoId)
    }
    next()
  }

  def getTodos(request: Request, response: Response, next: NextFunction) = {
    response.send(todos)
    next()
  }

  def updateTodo(request: Request, response: Response, next: NextFunction) = {
    request.bodyAs[Todo] match {
      case todo if todo.hasId && todo.hasTitle =>
        todos.indexWhereOpt(_.id == todo.id) match {
          case Some(index) =>
            todos(index) = todo
            response.send(todos)
          case None =>
            response.notFound(todo.id)
        }
      case todo =>
        response.badRequest(todo)
    }
    next()
  }

  def updateTodos(request: Request, response: Response, next: NextFunction) = {
    for {
      todo <- request.bodyAs[js.Array[Todo]].filter(_.isComplete)
      index <- todos.indexWhereOpt(_.id == todo.id)
    } todos(index) = todo

    response.send(todos)
    next()
  }

}
