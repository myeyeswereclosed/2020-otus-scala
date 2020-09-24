package ru.otus.scala.route

import akka.http.scaladsl.server.Route

trait Router {
  def route: Route
}
