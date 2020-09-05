package ru.otus.scala.book.router

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{JavaUUID, as, complete, entity, path, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import play.api.libs.json.{Json, OFormat}
import ru.otus.scala.book.model.CreateComment.{BookNotFound, CommentCreated, CreateCommentRequest}
import ru.otus.scala.book.model.domain.Comment.{BookComment, CommentText}
import ru.otus.scala.book.model.domain.{Author, Book}
import ru.otus.scala.book.service.comment.CommentService
import ru.otus.scala.route.Router

class CommentRouter(service: CommentService) extends Router {
  implicit lazy val authorFormat: OFormat[Author] = Json.format
  implicit lazy val bookFormat: OFormat[Book] = Json.format
  implicit lazy val commentTextFormat: OFormat[CommentText] = Json.format
  implicit lazy val commentFormat: OFormat[BookComment] = Json.format
  implicit lazy val commentRequestFormat: OFormat[CreateCommentRequest] = Json.format

  def route: Route = pathPrefix("book") { createComment }

  private def createComment: Route =
    (post & path(JavaUUID / "comment") & entity(as[CommentText])) {
      (bookId, comment) =>
        service.create(CreateCommentRequest(bookId, comment.text)) match {
          case CommentCreated(comment) => complete(comment)
          case BookNotFound(_) => complete(StatusCodes.NotFound)
        }
    }
}
