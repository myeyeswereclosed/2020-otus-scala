package ru.otus.scala.route
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteConcatenation._

class RouterImpl(useCasesRouters: Seq[Router]) extends Router {
  def route: Route = useCasesRouters.map(_.route).reduce(_ ~ _)
}
