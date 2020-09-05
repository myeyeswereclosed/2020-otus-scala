package ru.otus.scala.book.service

import java.util.UUID

import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import ru.otus.scala.book.dao.book.BookDao
import ru.otus.scala.book.dao.comment.CommentDao
import ru.otus.scala.book.model.AuthorsByYearOfPublishing.{AuthorsByYearOfPublishingRequest, AuthorsByYearOfPublishingResponse}
import ru.otus.scala.book.model.domain.Author
import ru.otus.scala.book.service.author.AuthorServiceImpl

class AuthorServiceImplTest extends AnyFreeSpec with MockFactory {
  "AuthorServiceTest tests" - {
    "getAllPublishedIn" - {
      val year = 2007

      val author = Author(Some(UUID.randomUUID()), "Some", "Author")

      val bookDao = mock[BookDao]
      val commentDao = stub[CommentDao]

      (bookDao.findAuthorsPublishedIn _).expects(year).returns(Seq(author))

      val service = new AuthorServiceImpl(bookDao, commentDao)

      service.getAllPublishedIn(AuthorsByYearOfPublishingRequest(year)) shouldBe
        AuthorsByYearOfPublishingResponse(Seq(author))
    }

    "getAllCommentedMoreThan" in {
      val commentsNumber = 10

      val author = Author(Some(UUID.randomUUID()), "Some", "Author")

      val bookDao = stub[BookDao]
      val commentDao = mock[CommentDao]

      (commentDao.findAuthorsCommentedMoreThan _).expects(commentsNumber).returns(Seq(author))

      val service = new AuthorServiceImpl(bookDao, commentDao)

      service.getAllCommentedMoreThan(commentsNumber) shouldBe Seq(author)
    }
  }
}
