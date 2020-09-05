package ru.otus.scala.book.service

import java.util.UUID

import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import ru.otus.scala.book.dao.author.AuthorDao
import ru.otus.scala.book.dao.book.BookDao
import ru.otus.scala.book.model.AddBookAuthor.AddAuthorRequest
import ru.otus.scala.book.model.AddBookAuthor.AddAuthorResponse.AuthorAdded
import ru.otus.scala.book.model.AllBooks.{AllBooksRequest, AllBooksResponse}
import ru.otus.scala.book.model.BooksByAuthorLastName.{BooksByAuthorLastNameRequest, BooksByAuthorLastNameResponse}
import ru.otus.scala.book.model.CreateBook.{BookCreated, CreateBookRequest}
import ru.otus.scala.book.model.DeleteBook.DeleteBookResponse.BookDeleted
import ru.otus.scala.book.model.DeleteBook.{DeleteBookRequest, DeleteBookResponse}
import ru.otus.scala.book.model.GetBook.GetBookRequest
import ru.otus.scala.book.model.GetBook.GetBookResponse.{BookFound, BookNotFound}
import ru.otus.scala.book.model.UpdateBook.UpdateBookResponse.{BookUpdated, CantUpdateBookWithoutId}
import ru.otus.scala.book.model.UpdateBook.{UpdateBookRequest, UpdateBookResponse}
import ru.otus.scala.book.model.domain.{Author, Book}
import ru.otus.scala.book.service.book.BookServiceImpl

class BookServiceImplTest extends AnyFreeSpec with MockFactory {

  private val firstName = "MyFirstName"
  private val lastName = "MyLastName"

  private val author = Author(Some(UUID.randomUUID()), firstName, lastName)

  private val book =
    Book(id = None, title = "GreatBook", authors = Seq(), pagesNumber = 456, yearOfPublishing = Some(1998))
  private val bookId = UUID.randomUUID()

  private val bookStored =
    Book(id = Some(bookId), book.title, book.authors, book.pagesNumber, book.yearOfPublishing)

  "BookServiceTest tests" - {
    "create" - {
      "should create book without authors" in {
        val bookDao = mock[BookDao]
        val authorDao = stub[AuthorDao]
        val service = new BookServiceImpl(bookDao, authorDao)

        (bookDao.create _).expects(book).returns(bookStored)

        service.create(CreateBookRequest(book)) shouldBe BookCreated(bookStored)
        (authorDao.findByFirstAndLastName _).verify(*, *).never()
      }

      "should create book with new authors" in {
        val bookDao = mock[BookDao]
        val authorDao = mock[AuthorDao]

        val authors = Seq(Author(None, firstName, lastName))

        (authorDao.addAll _).expects(authors).returns(authors)

        val bookWithAuthors = book.copy(authors = Seq(Author(None, firstName, lastName)))
        val bookWithAuthorsStored = bookStored.copy(authors = Seq(Author(None, firstName, lastName)))

        val service = new BookServiceImpl(bookDao, authorDao)

        (bookDao.create _).expects(bookWithAuthors).returns(bookWithAuthorsStored)

        service.create(CreateBookRequest(bookWithAuthors)) shouldBe BookCreated(bookWithAuthorsStored)
      }
    }

    "getAll" in {
      val bookDao = mock[BookDao]
      val authorDao = stub[AuthorDao]

      val page = 0
      val size = 10

      (bookDao.findAll _).expects(page, size).returns(Seq(bookStored))

      val service = new BookServiceImpl(bookDao, authorDao)

      service.getAll(AllBooksRequest(page, size)) shouldBe AllBooksResponse(Seq(bookStored))
    }

    "getBook" - {
      "get stored book" in {
        val bookDao = mock[BookDao]
        val authorDao = stub[AuthorDao]

        (bookDao.findById _).expects(bookId).returns(Some(bookStored))

        val service = new BookServiceImpl(bookDao, authorDao)

        service.get(GetBookRequest(bookId)) shouldBe BookFound(bookStored)
      }

      "get non-stored book" in {
        val bookDao = mock[BookDao]
        val authorDao = stub[AuthorDao]

        (bookDao.findById _).expects(bookId).returns(None)

        val service = new BookServiceImpl(bookDao, authorDao)

        service.get(GetBookRequest(bookId)) shouldBe BookNotFound(bookId)
      }
    }

    "updateBook" - {
      "update stored book" in {
        val bookDao = mock[BookDao]
        val authorDao = stub[AuthorDao]

        (bookDao.update _).expects(bookStored).returns(Some(bookStored))

        val service = new BookServiceImpl(bookDao, authorDao)

        service.update(UpdateBookRequest(bookStored)) shouldBe BookUpdated(bookStored)
      }

      "update non stored book" in {
        val bookDao = mock[BookDao]
        val authorDao = stub[AuthorDao]

        (bookDao.update _).expects(bookStored).returns(None)

        val service = new BookServiceImpl(bookDao, authorDao)

        service.update(UpdateBookRequest(bookStored)) shouldBe UpdateBookResponse.BookNotFound(bookId)
      }

      "update book without id" in {
        val bookDao = mock[BookDao]
        val authorDao = stub[AuthorDao]

        val service = new BookServiceImpl(bookDao, authorDao)

        service.update(UpdateBookRequest(book)) shouldBe CantUpdateBookWithoutId
      }
    }

    "delete" - {
      "delete stored book" in {
        val bookDao = mock[BookDao]
        val authorDao = stub[AuthorDao]

        (bookDao.delete _).expects(bookId).returns(Some(bookStored))

        val service = new BookServiceImpl(bookDao, authorDao)

        service.delete(DeleteBookRequest(bookId)) shouldBe BookDeleted(bookStored)
      }

      "delete non stored book" in {
        val bookDao = mock[BookDao]
        val authorDao = stub[AuthorDao]

        (bookDao.delete _).expects(bookId).returns(None)

        val service = new BookServiceImpl(bookDao, authorDao)

        service.delete(DeleteBookRequest(bookId)) shouldBe DeleteBookResponse.BookNotFound(bookId)
      }
    }

    "getBooksByAuthorsLastName" in {
      val bookDao = mock[BookDao]
      val authorDao = stub[AuthorDao]

      (bookDao.findAllByAuthorLastName _).expects(lastName).returns(Seq(bookStored))

      val service = new BookServiceImpl(bookDao, authorDao)

      service.getAllByAuthorLastName(BooksByAuthorLastNameRequest(lastName)) shouldBe
        BooksByAuthorLastNameResponse(Seq(bookStored))
    }

//    "getAuthorsByYearOfPublishing" in {
//      val bookDao = mock[BookDao]
//      val authorDao = stub[AuthorDao]
//
//      (bookDao.findAuthorsPublishedIn _).expects(book.yearOfPublishing.get).returns(Seq(author))
//
//      val service = new BookServiceImpl(bookDao, authorDao)
//
//      service
//        .getAuthorsPublishedIn(AuthorsByYearOfPublishingRequest(book.yearOfPublishing.get)) shouldBe
//        AuthorsByYearOfPublishingResponse(Seq(author))
//    }

    "addAuthor" - {
      "add non-stored author" in {
        val bookDao = mock[BookDao]
        val authorDao = mock[AuthorDao]

        val newAuthor = Author(None, "Another", "LastName")
        val newAuthorStored = newAuthor.copy(id = Some(UUID.randomUUID()))

        val bookUpdated = bookStored.copy(authors = Seq(newAuthorStored))

        (bookDao.findById _).expects(bookId).returns(Some(bookStored))
        (bookDao.update _).expects(bookUpdated).returns(Some(bookUpdated))
        (authorDao.findByFirstAndLastName _).expects(newAuthor.firstName, newAuthor.lastName).returns(None)
        (authorDao.create _).expects(newAuthor).returns(newAuthorStored)

        val service = new BookServiceImpl(bookDao, authorDao)

        service.addAuthor(AddAuthorRequest(bookId, newAuthor)) shouldBe AuthorAdded(bookUpdated)
      }

      "ignore adding existing book author" in {
        val bookDao = mock[BookDao]
        val authorDao = mock[AuthorDao]

        val newAuthor = Author(Some(UUID.randomUUID()), "Another", "LastName")

        val authoredBook = bookStored.copy(authors = Seq(author, newAuthor))

        (bookDao.findById _).expects(bookId).returns(Some(authoredBook))
        (authorDao.findByFirstAndLastName _).expects(newAuthor.firstName, newAuthor.lastName).never()
        (authorDao.create _).expects(newAuthor).never()

        val service = new BookServiceImpl(bookDao, authorDao)

        service.addAuthor(AddAuthorRequest(bookId, newAuthor)) shouldBe AuthorAdded(authoredBook)
      }

      "add stored author as author of book" in {
        val bookDao = mock[BookDao]
        val authorDao = mock[AuthorDao]

        val bookUpdated = bookStored.copy(authors = Seq(author))

        (bookDao.findById _).expects(bookId).returns(Some(bookStored))
        (bookDao.update _).expects(bookUpdated).returns(Some(bookUpdated))
        (authorDao.findByFirstAndLastName _).expects(author.firstName, author.lastName).returns(Some(author))
        (authorDao.create _).expects(author).never()

        val service = new BookServiceImpl(bookDao, authorDao)

        service.addAuthor(AddAuthorRequest(bookId, Author(None, author.firstName, author.lastName))) shouldBe
          AuthorAdded(bookUpdated)
      }
    }
  }
}
