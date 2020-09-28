package ru.otus.scala.router

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteConcatenation._

class AppRouter(useCasesRouters: Seq[Router]) extends Router {
  def route: Route = useCasesRouters.map(_.route).reduce(_ ~ _)
}
