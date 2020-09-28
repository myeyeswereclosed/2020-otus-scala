package ru.otus.scala.router

import java.util.UUID

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import play.api.libs.json.{Json, OFormat}
import ru.otus.scala.model.CreateComment.{CommentCreated, CreateCommentRequest}
import ru.otus.scala.model.domain.AppAuthor.Author
import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.model.domain.BookComment.{BookComment, CommentText}
import ru.otus.scala.service.comment.CommentService

import scala.concurrent.Future

class CommentRouteTest extends AnyFreeSpec with ScalatestRouteTest with MockFactory {
  implicit lazy val authorFormat: OFormat[Author] = Json.format
  implicit lazy val bookFormat: OFormat[AppBook] = Json.format
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
          AppBook(Some(bookId), "Just a book", Seq(), 100, Some(2007)),
        )
      val service = mock[CommentService]

      (service.create _)
        .expects(CreateCommentRequest(bookId, comment.text))
        .returns(Future.successful(CommentCreated(bookComment)))

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
