package ru.otus.scala.service

import java.util.UUID

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.{Futures, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import ru.otus.scala.model.CreateComment.{BookNotFound, CommentCreated, CreateCommentRequest}
import ru.otus.scala.model.domain.{AppBook, BookComment}
import ru.otus.scala.repository.{BookRepository, CommentRepository}
import ru.otus.scala.service.comment.CommentServiceImpl

import scala.concurrent.{ExecutionContext, Future}

class CommentServiceImplTest extends AnyFreeSpec with MockFactory with ScalaFutures {
  implicit val ec = ExecutionContext.global

  val bookId: UUID = UUID.randomUUID()

  val book: AppBook =
    AppBook(
      id = Some(bookId),
      title = "SomeBook",
      authors = Seq(),
      pagesNumber = 109,
      yearOfPublishing = Some(2014)
    )

  val comment: BookComment = BookComment(id = None, text = "Just a comment", book = book)
  val commentStored: BookComment = comment.copy(id = Some(UUID.randomUUID()))

  "CommentServiceTest tests" - {
    "create" - {
      "create comment to stored book" in {
        val bookDao = mock[BookRepository]
        val commentDao = mock[CommentRepository]

        (bookDao.findById _).expects(bookId).returns(Future.successful(Some(book)))
        (commentDao.create _)
          .expects( where {
            expected: BookComment =>
              expected.text == comment.text && expected.book == book
          } )
          .returns(Future.successful(commentStored))

        val service = new CommentServiceImpl(bookDao, commentDao)

        service.create(CreateCommentRequest(bookId, comment.text)).futureValue shouldBe CommentCreated(commentStored)
      }

      "create comment to non-stored book" in {
        val bookDao = mock[BookRepository]
        val commentDao = mock[CommentRepository]

        (bookDao.findById _).expects(bookId).returns(Future.successful(None))
        (commentDao.create _).expects(*).never()

        val service = new CommentServiceImpl(bookDao, commentDao)

        service.create(CreateCommentRequest(bookId, comment.text)).futureValue shouldBe BookNotFound(bookId)
      }
    }
  }
}
