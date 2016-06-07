package com.github.ldaniels528.todomvc.services

import com.github.ldaniels528.meansjs.angularjs._
import com.github.ldaniels528.meansjs.angularjs.http.Http
import com.github.ldaniels528.todomvc.models.Todo

import scala.scalajs.js

/**
  * Todo Storage Service
  * @author lawrence.daniels@gmail.com
  */
class TodoStorageService($http: Http) extends Service {

  def create(todo: Todo) = $http.post[js.Array[Todo]]("/api/todo", data = todo)

  def delete(todo: Todo) = $http.delete[js.Array[Todo]](s"/api/todo/${todo.id}")

  def deleteCompleted() = $http.delete[js.Array[Todo]](s"/api/todos/completed")

  def get(todo: Todo) = $http.get[js.Array[Todo]](s"/api/todo/${todo.id}")

  def getAll = $http.get[js.Array[Todo]](s"/api/todos")

  def update(todo: Todo) = $http.put[js.Array[Todo]]("/api/todo", data = todo)

  def update(todos: js.Array[Todo]) = $http.put[js.Array[Todo]]("/api/todos", data = todos)

}
