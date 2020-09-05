package ru.otus.scala.book.router

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import play.api.libs.json.{Json, OFormat}
import ru.otus.scala.book.model.AuthorsByYearOfPublishing.{AuthorsByYearOfPublishingRequest, AuthorsByYearOfPublishingResponse}
import ru.otus.scala.book.model.domain.Author
import ru.otus.scala.book.service.author.AuthorService


class AuthorRouterTest extends AnyFreeSpec with ScalatestRouteTest with MockFactory {
  implicit lazy val authorFormat: OFormat[Author] = Json.format

  val author: Author = Author(Some(UUID.randomUUID()), "Some", "Author")

  "Author routes tests" - {
    "authors publishedIn" in {
      val year = 2000

      val service = mock[AuthorService]
      val router = new AuthorRouter(service)

      (service.getAllPublishedIn _)
        .expects(AuthorsByYearOfPublishingRequest(year))
        .returns(AuthorsByYearOfPublishingResponse(Seq(author)))

      Get(s"/authors?publishedIn=$year") ~> router.route ~> check {
        handled shouldBe true
        responseAs[Seq[Author]] shouldBe Seq(author)
        status shouldBe StatusCodes.OK
      }
    }

    "authors commented more than" in {
      val commentsNumber = 5

      val service = mock[AuthorService]
      val router = new AuthorRouter(service)

      (service.getAllCommentedMoreThan _)
        .expects(commentsNumber)
        .returns(Seq(author))

      Get(s"/authors?commentedMoreThan=$commentsNumber") ~> router.route ~> check {
        handled shouldBe true
        responseAs[Seq[Author]] shouldBe Seq(author)
        status shouldBe StatusCodes.OK
      }
    }
  }
}
