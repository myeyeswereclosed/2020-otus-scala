package ru.otus.scala.book.router

import java.util.UUID

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import play.api.libs.json.{Json, OFormat}
import ru.otus.scala.book.model.CreateComment.{CommentCreated, CreateCommentRequest}
import ru.otus.scala.book.model.domain.Comment.{BookComment, CommentText}
import ru.otus.scala.book.model.domain.{Author, Book}
import ru.otus.scala.book.service.comment.CommentService

class CommentRouteTest extends AnyFreeSpec with ScalatestRouteTest with MockFactory {
  implicit lazy val authorFormat: OFormat[Author] = Json.format
  implicit lazy val bookFormat: OFormat[Book] = Json.format
  implicit lazy val commentTextFormat: OFormat[CommentText] = Json.format
  implicit lazy val commentFormat: OFormat[BookComment] = Json.format
  implicit lazy val commentRequestFormat: OFormat[CreateCommentRequest] = Json.format

  "Comment router tests" - {
    "create comment" in {
      val bookId = UUID.randomUUID()
      val comment = CommentText("good book")
      val bookComment =
        BookComment(
          Some(UUID.randomUUID()),
          comment.text,
          Book(Some(bookId), "Just a book", Seq(), 100, Some(2007))
        )
      val service = mock[CommentService]

      (service.create _)
        .expects(CreateCommentRequest(bookId, comment.text))
        .returns(CommentCreated(bookComment))

      val router = new CommentRouter(service)

      Post(
        s"/book/$bookId/comment",
        HttpEntity(MediaTypes.`application/json`, Json.toBytes(Json.toJson(comment)))
      ) ~> router.route ~> check {
        handled shouldBe true
        responseAs[BookComment] shouldBe bookComment
        status shouldBe StatusCodes.OK
      }
    }
  }
}
