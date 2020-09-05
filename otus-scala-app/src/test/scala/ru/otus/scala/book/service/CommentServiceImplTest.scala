package ru.otus.scala.book.service

import java.util.UUID

import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import ru.otus.scala.book.dao.book.BookDao
import ru.otus.scala.book.dao.comment.CommentDao
import ru.otus.scala.book.model.CreateComment.{BookNotFound, CommentCreated, CreateCommentRequest}
import ru.otus.scala.book.model.domain.Book
import ru.otus.scala.book.model.domain.Comment.BookComment
import ru.otus.scala.book.service.comment.CommentServiceImpl

class CommentServiceImplTest extends AnyFreeSpec with MockFactory {
  val bookId: UUID = UUID.randomUUID()

  val book: Book =
    Book(
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
        val bookDao = mock[BookDao]
        val commentDao = mock[CommentDao]

        (bookDao.findById _).expects(bookId).returns(Some(book))
        (commentDao.create _)
          .expects( where {
            expected:BookComment =>
              expected.text == comment.text && expected.book == book
          } )
          .returns(commentStored)

        val service = new CommentServiceImpl(bookDao, commentDao)

        service.create(CreateCommentRequest(bookId, comment.text)) shouldBe CommentCreated(commentStored)
      }

      "create comment to non-stored book" in {
        val bookDao = mock[BookDao]
        val commentDao = mock[CommentDao]

        (bookDao.findById _).expects(bookId).returns(None)
        (commentDao.create _).expects(*).never()

        val service = new CommentServiceImpl(bookDao, commentDao)

        service.create(CreateCommentRequest(bookId, comment.text)) shouldBe BookNotFound(bookId)
      }
    }
  }
}
