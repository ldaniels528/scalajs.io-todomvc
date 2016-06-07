package com.github.ldaniels528.todomvc.models

import com.github.ldaniels528.meansjs.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * Todo Model
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait Todo extends js.Object {
  var id: String = js.native
  var title: String = js.native
  var completed: Boolean = js.native
}

/**
  * Todo Companion
  * @author lawrence.daniels@gmail.com
  */
object Todo {

  def apply(title: String = "",
            completed: Boolean = false) = {
    val todo = New[Todo]
    todo.title = title
    todo.completed = completed
    todo
  }

  /**
    * Todo Extensions
    * @param todo the given todo
    */
  implicit class TodoExtensions(val todo: Todo) extends AnyVal {

    @inline
    def copy(id: js.UndefOr[String] = js.undefined,
             title: js.UndefOr[String] = js.undefined,
             completed: js.UndefOr[Boolean] = js.undefined) = {
      val newTodo = Todo()
      newTodo.id = id getOrElse todo.id
      newTodo.title = title getOrElse todo.title
      newTodo.completed = completed getOrElse todo.completed
      newTodo
    }

    @inline
    def hasId = Option(todo).flatMap(t => Option(t.id)).exists(_.trim.nonEmpty)

    @inline
    def hasTitle = Option(todo).flatMap(t => Option(t.title)).exists(_.trim.nonEmpty)

    @inline
    def isComplete = hasId && hasTitle

  }

}