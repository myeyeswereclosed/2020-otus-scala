package ru.otus.scala.service

import java.util.UUID

import cats.effect.{ContextShift, IO}
import doobie.util.ExecutionContexts
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import ru.otus.scala.model.AddBookAuthor.AddAuthorRequest
import ru.otus.scala.model.AddBookAuthor.AddAuthorResponse.AuthorAdded
import ru.otus.scala.model.AllBooks.{AllBooksRequest, AllBooksResponse}
import ru.otus.scala.model.BooksByAuthorLastName.{BooksByAuthorLastNameRequest, BooksByAuthorLastNameResponse}
import ru.otus.scala.model.CreateBook.{BookCreated, CreateBookRequest}
import ru.otus.scala.model.DeleteBook.DeleteBookResponse.BookDeleted
import ru.otus.scala.model.DeleteBook.{DeleteBookRequest, DeleteBookResponse}
import ru.otus.scala.model.GetBook.GetBookRequest
import ru.otus.scala.model.GetBook.GetBookResponse.{BookFound, BookNotFound}
import ru.otus.scala.model.UpdateBook.UpdateBookResponse.{BookUpdated, CantUpdateBookWithoutId}
import ru.otus.scala.model.UpdateBook.{UpdateBookRequest, UpdateBookResponse}
import ru.otus.scala.model.DeleteBook.DeleteBookResponse
import ru.otus.scala.model.UpdateBook.UpdateBookResponse
import ru.otus.scala.model.UpdateBook.UpdateBookResponse.CantUpdateBookWithoutId
import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.model.domain.author.Author
import ru.otus.scala.repository.{AuthorRepository, BookRepository}
import ru.otus.scala.service.book.BookServiceImpl

import scala.concurrent.{ExecutionContext, Future}

class BookServiceImplTest extends AnyFreeSpec with MockFactory with ScalaFutures {
  implicit val ec: ExecutionContext = ExecutionContext.global
  private val firstName = "MyFirstName"
  private val lastName = "MyLastName"

  private val author = Author(Some(UUID.randomUUID()), firstName, lastName)

  private val book =
    AppBook(id = None, title = "GreatBook", authors = Seq(), pagesNumber = 456, yearOfPublishing = Some(1998))
  private val bookId = UUID.randomUUID()

  private val bookStored =
    AppBook(id = Some(bookId), book.title, book.authors, book.pagesNumber, book.yearOfPublishing)

  "BookServiceTest tests" - {
    "create" - {
      "should create book without authors" in {
        val bookDao = mock[BookRepository]
        val authorDao = stub[AuthorRepository]
        val service = new BookServiceImpl(bookDao, authorDao)

        (bookDao.create _).expects(book).returns(Future.successful(bookStored))

        service.create(CreateBookRequest(book)).futureValue shouldBe BookCreated(bookStored)
        (authorDao.findByFirstAndLastName _).verify(*, *).never()
      }

//      "should create book with new authors" in {
//        val bookDao = mock[BookRepository]
//        val authorDao = mock[AuthorRepository]
//
//        val authors = Seq(Author(None, firstName, lastName))
//
//        (authorDao.addAll _).expects(authors).returns(authors)
//
//        val bookWithAuthors = book.copy(authors = Seq(Author(None, firstName, lastName)))
//        val bookWithAuthorsStored = bookStored.copy(authors = Seq(Author(None, firstName, lastName)))
//
//        val service = new BookServiceImpl(bookDao, authorDao)
//
//        (bookDao.create _).expects(bookWithAuthors).returns(bookWithAuthorsStored)
//
//        service.create(CreateBookRequest(bookWithAuthors)) shouldBe BookCreated(bookWithAuthorsStored)
//      }
    }

    "getAll" in {
      val bookDao = mock[BookRepository]
      val authorDao = stub[AuthorRepository]

      val page = 0
      val size = 10

      (bookDao.findAll _).expects(page, size).returns(Future.successful(Seq(bookStored)))

      val service = new BookServiceImpl(bookDao, authorDao)

      service.getAll(AllBooksRequest(page, size)).futureValue shouldBe AllBooksResponse(Seq(bookStored))
    }

    "getBook" - {
      "get stored book" in {
        val bookDao = mock[BookRepository]
        val authorDao = stub[AuthorRepository]

        (bookDao.findById _).expects(bookId).returns(Future.successful(Some(bookStored)))

        val service = new BookServiceImpl(bookDao, authorDao)

        service.get(GetBookRequest(bookId)).futureValue shouldBe BookFound(bookStored)
      }

      "get non-stored book" in {
        val bookDao = mock[BookRepository]
        val authorDao = stub[AuthorRepository]

        (bookDao.findById _).expects(bookId).returns(Future.successful(None))

        val service = new BookServiceImpl(bookDao, authorDao)

        service.get(GetBookRequest(bookId)).futureValue shouldBe BookNotFound(bookId)
      }
    }

    "updateBook" - {
      "update stored book" in {
        val bookDao = mock[BookRepository]
        val authorDao = stub[AuthorRepository]

        (bookDao.update _).expects(bookStored).returns(Future.successful(Some(bookStored)))

        val service = new BookServiceImpl(bookDao, authorDao)

        service.update(UpdateBookRequest(bookStored)).futureValue shouldBe BookUpdated(bookStored)
      }

      "update non stored book" in {
        val bookDao = mock[BookRepository]
        val authorDao = stub[AuthorRepository]

        (bookDao.update _).expects(bookStored).returns(Future.successful(None))

        val service = new BookServiceImpl(bookDao, authorDao)

        service.update(UpdateBookRequest(bookStored)).futureValue shouldBe UpdateBookResponse.BookNotFound(bookId)
      }

      "update book without id" in {
        val bookDao = mock[BookRepository]
        val authorDao = stub[AuthorRepository]

        val service = new BookServiceImpl(bookDao, authorDao)

        service.update(UpdateBookRequest(book)).futureValue shouldBe CantUpdateBookWithoutId
      }
    }

    "delete" - {
      "delete stored book" in {
        val bookDao = mock[BookRepository]
        val authorDao = stub[AuthorRepository]

        (bookDao.delete _).expects(bookId).returns(Future.successful(bookStored.id))

        val service = new BookServiceImpl(bookDao, authorDao)

        service.delete(DeleteBookRequest(bookId)).futureValue shouldBe BookDeleted(bookStored.id.get)
      }

      "delete non stored book" in {
        val bookDao = mock[BookRepository]
        val authorDao = stub[AuthorRepository]

        (bookDao.delete _).expects(bookId).returns(Future.successful(None))

        val service = new BookServiceImpl(bookDao, authorDao)

        service.delete(DeleteBookRequest(bookId)).futureValue shouldBe DeleteBookResponse.BookNotFound(bookId)
      }
    }

    "getBooksByAuthorsLastName" in {
      val bookDao = mock[BookRepository]
      val authorDao = stub[AuthorRepository]

      (bookDao.findAllByAuthorLastName _)
        .expects(lastName)
        .returns(Future.successful(Seq(bookStored)))

      val service = new BookServiceImpl(bookDao, authorDao)

      service.getAllByAuthorLastName(BooksByAuthorLastNameRequest(lastName)).futureValue shouldBe
        BooksByAuthorLastNameResponse(Seq(bookStored))
    }
    
    "addAuthor" - {
      "add non-stored author" in {
        val bookDao = mock[BookRepository]
        val authorDao = mock[AuthorRepository]

        val newAuthor = Author(None, "Another", "LastName")
        val newAuthorStored = newAuthor.copy(id = Some(UUID.randomUUID()))

        val bookUpdated = bookStored.copy(authors = Seq(newAuthorStored))

        (bookDao.findById _).expects(bookId).returns(Future.successful(Some(bookStored)))
        (bookDao.update _).expects(bookUpdated).returns(Future.successful(Some(bookUpdated)))
        (authorDao.findByFirstAndLastName _)
          .expects(newAuthor.firstName, newAuthor.lastName)
          .returns(Future.successful(None))
        (authorDao.create _).expects(newAuthor).returns(Future.successful(newAuthorStored))

        val service = new BookServiceImpl(bookDao, authorDao)

        service.addAuthor(AddAuthorRequest(bookId, newAuthor)).futureValue shouldBe AuthorAdded(bookUpdated)
      }

      "ignore adding existing book author" in {
        val bookDao = mock[BookRepository]
        val authorDao = mock[AuthorRepository]

        val newAuthor = Author(Some(UUID.randomUUID()), "Another", "LastName")

        val authoredBook = bookStored.copy(authors = Seq(author, newAuthor))

        (bookDao.findById _).expects(bookId).returns(Future.successful(Some(authoredBook)))
        (authorDao.findByFirstAndLastName _).expects(newAuthor.firstName, newAuthor.lastName).never()
        (authorDao.create _).expects(newAuthor).never()

        val service = new BookServiceImpl(bookDao, authorDao)

        service.addAuthor(AddAuthorRequest(bookId, newAuthor)).futureValue shouldBe AuthorAdded(authoredBook)
      }

      "add stored author as author of book" in {
        val bookDao = mock[BookRepository]
        val authorDao = mock[AuthorRepository]

        val bookUpdated = bookStored.copy(authors = Seq(author))

        (bookDao.findById _)
          .expects(bookId)
          .returns(Future.successful(Some(bookStored)))
        (bookDao.update _)
          .expects(bookUpdated)
          .returns(Future.successful(Some(bookUpdated)))
        (authorDao.findByFirstAndLastName _)
          .expects(author.firstName, author.lastName)
          .returns(Future.successful(Some(author)))
        (authorDao.create _).expects(author).never()

        val service = new BookServiceImpl(bookDao, authorDao)

        service
          .addAuthor(
            AddAuthorRequest(
              bookId,
              Author(None, author.firstName, author.lastName)
            )
          ).futureValue shouldBe AuthorAdded(bookUpdated)
      }
    }
  }
}
