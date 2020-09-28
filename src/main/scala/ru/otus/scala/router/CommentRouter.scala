package ru.otus.scala.router

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{JavaUUID, as, complete, entity, path, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import play.api.libs.json.{Json, OFormat}
import ru.otus.scala.model.CreateComment.{BookNotFound, CommentCreated, CreateCommentRequest}
import ru.otus.scala.model.domain.{AppAuthor, AppBook}
import AppAuthor.Author
import ru.otus.scala.model.domain.BookComment.{BookComment, CommentText}
import ru.otus.scala.service.comment.CommentService

class CommentRouter(service: CommentService) extends Router {
  implicit lazy val authorFormat: OFormat[Author] = Json.format
  implicit lazy val bookFormat: OFormat[AppBook] = Json.format
  implicit lazy val commentTextFormat: OFormat[CommentText] = Json.format
  implicit lazy val commentFormat: OFormat[BookComment] = Json.format
  implicit lazy val commentRequestFormat: OFormat[CreateCommentRequest] = Json.format

  def route: Route = pathPrefix("book") { createComment }

  private def createComment: Route =
    (post & path(JavaUUID / "comment") & entity(as[CommentText])) {
      (bookId, comment) =>
        onSuccess(service.create(CreateCommentRequest(bookId, comment.text))) {
          case CommentCreated(comment) => complete(comment)
          case BookNotFound(_) => complete(StatusCodes.NotFound)
        }
    }
}
