package ru.otus.scala.greet.router

import akka.http.scaladsl.server.Directives.{get, parameter, path, _}
import akka.http.scaladsl.server.Route
import ru.otus.scala.greet.model.GreetRequest
import ru.otus.scala.greet.service.GreetingService
import ru.otus.scala.route.Router

class GreetRouter(service: GreetingService) extends Router {
  def route: Route = (get & path("greet" / Segment) & parameter(
    "isHuman".as[Boolean].?(default = true)
  )) {
    (name, isHuman) => complete(service.greet(GreetRequest(name, isHuman)).greeting)
  }
}
