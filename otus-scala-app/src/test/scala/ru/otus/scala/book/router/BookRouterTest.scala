package ru.otus.scala.book.router

import java.util.UUID

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import play.api.libs.json.{Json, OFormat}
import ru.otus.scala.book.model.AddBookAuthor.AddAuthorRequest
import ru.otus.scala.book.model.AddBookAuthor.AddAuthorResponse.AuthorAdded
import ru.otus.scala.book.model.AllBooks.{AllBooksRequest, AllBooksResponse}
import ru.otus.scala.book.model.BooksByAuthorLastName.{BooksByAuthorLastNameRequest, BooksByAuthorLastNameResponse}
import ru.otus.scala.book.model.CreateBook.{BookCreated, CreateBookRequest}
import ru.otus.scala.book.model.DeleteBook.DeleteBookRequest
import ru.otus.scala.book.model.DeleteBook.DeleteBookResponse.BookDeleted
import ru.otus.scala.book.model.GetBook.GetBookRequest
import ru.otus.scala.book.model.GetBook.GetBookResponse.BookFound
import ru.otus.scala.book.model.UpdateBook.UpdateBookRequest
import ru.otus.scala.book.model.UpdateBook.UpdateBookResponse.BookUpdated
import ru.otus.scala.book.model.domain.{Author, Book}
import ru.otus.scala.book.service.book.BookService

class BookRouterTest extends AnyFreeSpec with ScalatestRouteTest with MockFactory {
  implicit lazy val authorFormat: OFormat[Author] = Json.format
  implicit lazy val bookFormat: OFormat[Book] = Json.format

  val bookId: UUID = UUID.randomUUID()
  val book: Book =
    Book(
      None,
      title = "Just a book",
      authors = Seq(),
      pagesNumber = 100,
      yearOfPublishing = Some(2007)
    )

  val storedBook: Book =
    Book(id = Some(UUID.randomUUID()), book.title, book.authors, book.pagesNumber, book.yearOfPublishing)

  "Book router tests" - {
    "create book" in {
      val service = mock[BookService]

      val router = new BookRouter(service)

      (service.create _).expects(CreateBookRequest(book)).returns(BookCreated(storedBook))

      Post("/book",
        HttpEntity(MediaTypes.`application/json`, Json.toBytes(Json.toJson(book)))
      ) ~> router.route ~> check {
        handled shouldBe true
        status shouldBe StatusCodes.OK
        responseAs[Book] shouldBe storedBook
      }
    }

    "get all books" in {
      val page = 1
      val size = 100

      val service = mock[BookService]

      val router = new BookRouter(service)

      (service.getAll _).expects(AllBooksRequest(page, size)).returns(AllBooksResponse(Seq(storedBook)))

      Get(s"/books?page=$page&size=$size") ~> router.route ~> check {
        handled shouldBe true
        status shouldBe StatusCodes.OK
        responseAs[Seq[Book]] shouldBe Seq(storedBook)
      }
    }

    "get book" in {
      val service = mock[BookService]

      val router = new BookRouter(service)

      (service.get _).expects(GetBookRequest(bookId)).returns(BookFound(storedBook))

      Get(s"/book/$bookId") ~> router.route ~> check {
        handled shouldBe true
        status shouldBe StatusCodes.OK
        responseAs[Book] shouldBe storedBook
      }
    }

    "update book" in {
      val service = mock[BookService]

      val router = new BookRouter(service)

      (service.update _).expects(UpdateBookRequest(storedBook)).returns(BookUpdated(storedBook))

      Put(
        "/book",
        HttpEntity(
          MediaTypes.`application/json`,
          Json.toBytes(Json.toJson(storedBook))
        )
      ) ~> router.route ~> check {
        handled shouldBe true
        status shouldBe StatusCodes.OK
        responseAs[Book] shouldBe storedBook
      }
    }

    "delete book" in {
      val service = mock[BookService]

      val router = new BookRouter(service)

      (service.delete _).expects(DeleteBookRequest(bookId)).returns(BookDeleted(storedBook))

      Delete(s"/book/$bookId") ~> router.route ~> check {
        handled shouldBe true
        status shouldBe StatusCodes.OK
        responseAs[Book] shouldBe storedBook
      }
    }

    "add author" in {
      val service = mock[BookService]

      val router = new BookRouter(service)
      val author = Author(None, "Some", "Author")
      val authorStored = author.copy(id = Some(UUID.randomUUID()))

      val authoredBook = book.copy(authors = Seq(authorStored))

      (service.addAuthor _).expects(AddAuthorRequest(bookId, author)).returns(AuthorAdded(authoredBook))

      Post(
        s"/book/$bookId/author",
        HttpEntity(
          MediaTypes.`application/json`,
          Json.toBytes(Json.toJson(author))
        )
      ) ~> router.route ~> check {
        handled shouldBe true
        status shouldBe StatusCodes.OK
        responseAs[Book] shouldBe authoredBook
      }
    }

    "books by author last name" in {
      val lastName = "JustLastName"

      val service = mock[BookService]

      val router = new BookRouter(service)

      (service.getAllByAuthorLastName _)
        .expects(BooksByAuthorLastNameRequest(lastName))
        .returns(BooksByAuthorLastNameResponse(Seq(storedBook)))

      Get(s"/books?authorLastName=$lastName") ~> router.route ~> check {
        handled shouldBe true
        status shouldBe StatusCodes.OK
        responseAs[Seq[Book]] shouldBe Seq(storedBook)
      }
    }

    "books by pages number greater than and authors with pages number less than" in {
      val greaterThan = 100
      val lessThan = 50

      val service = mock[BookService]

      val router = new BookRouter(service)

      (service.booksByPagesNumberGreaterThanAndAuthorsWithPagesNumberLessThan _)
        .expects(greaterThan, lessThan)
        .returns(Seq(storedBook))

      Get(s"/books?pagesNumberFrom=$greaterThan&otherAuthorBookPagesNumberTo=$lessThan") ~> router.route ~> check {
        handled shouldBe true
        status shouldBe StatusCodes.OK
        responseAs[Seq[Book]] shouldBe Seq(storedBook)
      }
    }
  }
}
