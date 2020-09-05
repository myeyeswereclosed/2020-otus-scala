package ru.otus.scala.book.router

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import play.api.libs.json.{Json, OFormat}
import ru.otus.scala.book.model.AuthorsByYearOfPublishing.AuthorsByYearOfPublishingRequest
import ru.otus.scala.book.model.domain.Author
import ru.otus.scala.book.service.author.AuthorService
import ru.otus.scala.route.Router

class AuthorRouter(service: AuthorService) extends Router {
  implicit lazy val authorFormat: OFormat[Author] = Json.format

  def route: Route = pathPrefix("authors") { publishedIn ~ commentedMoreThan }

  private def publishedIn: Route =
    (get & parameter("publishedIn".as[Int])) {
      year => complete(service.getAllPublishedIn(AuthorsByYearOfPublishingRequest(year)).authors)
    }

  private def commentedMoreThan: Route =
    (get & parameter("commentedMoreThan".as[Int])) {
      commentsNumber => complete(service.getAllCommentedMoreThan(commentsNumber))
    }
}
