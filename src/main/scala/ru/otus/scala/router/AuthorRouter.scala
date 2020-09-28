package ru.otus.scala.router

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import play.api.libs.json.{Json, OFormat}
import ru.otus.scala.model.AuthorsByYearOfPublishing.AuthorsByYearOfPublishingRequest
import ru.otus.scala.model.domain.AppAuthor
import AppAuthor.Author
import ru.otus.scala.service.author.AuthorService

class AuthorRouter(service: AuthorService) extends Router {
  implicit lazy val authorFormat: OFormat[Author] = Json.format

  def route: Route = pathPrefix("authors") { publishedIn ~ commentedMoreThan }

  private def publishedIn: Route =
    (get & parameter("publishedIn".as[Int])) {
      year =>
        onSuccess(service.getAllPublishedIn(AuthorsByYearOfPublishingRequest(year))) {
          response => complete(response.authors)
        }
    }

  private def commentedMoreThan: Route =
    (get & parameter("commentedMoreThan".as[Int])) {
      commentsNumber => complete(service.getAllCommentedMoreThan(commentsNumber))
    }
}
