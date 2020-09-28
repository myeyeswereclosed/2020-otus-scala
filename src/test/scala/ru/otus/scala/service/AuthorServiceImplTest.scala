package ru.otus.scala.service

import java.util.UUID

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import ru.otus.scala.model.AuthorsByYearOfPublishing.{AuthorsByYearOfPublishingRequest, AuthorsByYearOfPublishingResponse}
import ru.otus.scala.model.domain.AppAuthor
import ru.otus.scala.model.domain.AppAuthor.Author
import ru.otus.scala.repository.{AuthorRepository, BookRepository, CommentRepository}
import ru.otus.scala.service.author.AuthorServiceImpl

import scala.concurrent.{ExecutionContext, Future}

class AuthorServiceImplTest extends AnyFreeSpec with MockFactory with ScalaFutures {
  implicit val ec = ExecutionContext.global

  "AuthorServiceTest tests" - {
    "getAllPublishedIn" in {
      val year = 2007

      val author = Author(Some(UUID.randomUUID()), "Some", "Author")

      val authorRepository = mock[AuthorRepository]
      val commentRepository = stub[CommentRepository]

      (authorRepository.findPublishedIn _).expects(year).returns(Future.successful(Seq(author)))

      val service = new AuthorServiceImpl(authorRepository, commentRepository)

      service.getAllPublishedIn(AuthorsByYearOfPublishingRequest(year)).futureValue shouldBe
        AuthorsByYearOfPublishingResponse(Seq(author))
    }

    "getAllCommentedMoreThan" in {
      val commentsNumber = 10

      val author = Author(Some(UUID.randomUUID()), "Some", "Author")

      val authorRepository = stub[AuthorRepository]
      val commentRepository = mock[CommentRepository]

      (commentRepository.findAuthorsCommentedMoreThan _).expects(commentsNumber).returns(Seq(author))

      val service = new AuthorServiceImpl(authorRepository, commentRepository)

      service.getAllCommentedMoreThan(commentsNumber) shouldBe Seq(author)
    }
  }
}
