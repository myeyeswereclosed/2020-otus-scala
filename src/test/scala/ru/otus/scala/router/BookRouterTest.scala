package ru.otus.scala.router

import java.util.UUID

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import play.api.libs.json.{Json, OFormat}
import ru.otus.scala.model.AddBookAuthor.AddAuthorRequest
import ru.otus.scala.model.AddBookAuthor.AddAuthorResponse.AuthorAdded
import ru.otus.scala.model.AllBooks.{AllBooksRequest, AllBooksResponse}
import ru.otus.scala.model.BooksByAuthorLastName.{BooksByAuthorLastNameRequest, BooksByAuthorLastNameResponse}
import ru.otus.scala.model.CreateBook.{BookCreated, CreateBookRequest}
import ru.otus.scala.model.DeleteBook.DeleteBookRequest
import ru.otus.scala.model.DeleteBook.DeleteBookResponse.BookDeleted
import ru.otus.scala.model.GetBook.GetBookRequest
import ru.otus.scala.model.GetBook.GetBookResponse.BookFound
import ru.otus.scala.model.UpdateBook.UpdateBookRequest
import ru.otus.scala.model.UpdateBook.UpdateBookResponse.BookUpdated
import ru.otus.scala.model.domain.AppAuthor.Author
import ru.otus.scala.model.domain.{AppAuthor, AppBook}
import ru.otus.scala.service.book.BookService

import scala.concurrent.{ExecutionContext, Future}

class BookRouterTest extends AnyFreeSpec with ScalatestRouteTest with MockFactory {
  implicit lazy val authorFormat: OFormat[Author] = Json.format
  implicit lazy val bookFormat: OFormat[AppBook] = Json.format
  implicit val ec = ExecutionContext.global

  val bookId: UUID = UUID.randomUUID()
  val book: AppBook =
    AppBook(
      None,
      title = "Just a book",
      authors = Seq(),
      pagesNumber = 100,
      yearOfPublishing = Some(2007)
    )

  val storedBook: AppBook =
    AppBook(id = Some(UUID.randomUUID()), book.title, book.authors, book.pagesNumber, book.yearOfPublishing)

  "Book router tests" - {
    "create book" in {
      val service = mock[BookService]

      val router = new BookRouter(service)

      (service.create _)
        .expects(CreateBookRequest(book))
        .returns(Future.successful(BookCreated(storedBook)))

      Post("/book",
        HttpEntity(MediaTypes.`application/json`, Json.toBytes(Json.toJson(book)))
      ) ~> router.route ~> check {
        handled shouldBe true
        status shouldBe StatusCodes.OK
        responseAs[AppBook] shouldBe storedBook
      }
    }

    "get all books" in {
      val page = 1
      val size = 100

      val service = mock[BookService]

      val router = new BookRouter(service)

      (service.getAll _)
        .expects(AllBooksRequest(page, size))
        .returns(Future.successful(AllBooksResponse(Seq(storedBook))))

      Get(s"/books?page=$page&size=$size") ~> router.route ~> check {
        handled shouldBe true
        status shouldBe StatusCodes.OK
        responseAs[Seq[AppBook]] shouldBe Seq(storedBook)
      }
    }

    "get book" in {
      val service = mock[BookService]

      val router = new BookRouter(service)

      (service.get _).expects(GetBookRequest(bookId)).returns(Future.successful(BookFound(storedBook)))

      Get(s"/book/$bookId") ~> router.route ~> check {
        handled shouldBe true
        status shouldBe StatusCodes.OK
        responseAs[AppBook] shouldBe storedBook
      }
    }

    "update book" in {
      val service = mock[BookService]

      val router = new BookRouter(service)

      (service.update _)
        .expects(UpdateBookRequest(storedBook))
        .returns(Future.successful(BookUpdated(storedBook)))

      Put(
        "/book",
        HttpEntity(
          MediaTypes.`application/json`,
          Json.toBytes(Json.toJson(storedBook))
        )
      ) ~> router.route ~> check {
        handled shouldBe true
        status shouldBe StatusCodes.OK
        responseAs[AppBook] shouldBe storedBook
      }
    }

    "delete book" in {
      val service = mock[BookService]

      val router = new BookRouter(service)

      (service.delete _)
        .expects(DeleteBookRequest(bookId))
        .returns(Future.successful(BookDeleted(storedBook.id.get)))

      Delete(s"/book/$bookId") ~> router.route ~> check {
        handled shouldBe true
        status shouldBe StatusCodes.OK
        responseAs[UUID] shouldBe storedBook.id.get
      }
    }

    "add author" in {
      val service = mock[BookService]

      val router = new BookRouter(service)
      val author = Author(None, "Some", "Author")
      val authorStored = author.copy(id = Some(UUID.randomUUID()))

      val authoredBook = book.copy(authors = Seq(authorStored))

      (service.addAuthor _)
        .expects(AddAuthorRequest(bookId, author))
        .returns(Future.successful(AuthorAdded(authoredBook)))

      Post(
        s"/book/$bookId/author",
        HttpEntity(
          MediaTypes.`application/json`,
          Json.toBytes(Json.toJson(author))
        )
      ) ~> router.route ~> check {
        handled shouldBe true
        status shouldBe StatusCodes.OK
        responseAs[AppBook] shouldBe authoredBook
      }
    }

    "books by author last name" in {
      val lastName = "JustLastName"

      val service = mock[BookService]

      val router = new BookRouter(service)

      (service.getAllByAuthorLastName _)
        .expects(BooksByAuthorLastNameRequest(lastName))
        .returns(Future.successful(BooksByAuthorLastNameResponse(Seq(storedBook))))

      Get(s"/books?authorLastName=$lastName") ~> router.route ~> check {
        handled shouldBe true
        status shouldBe StatusCodes.OK
        responseAs[Seq[AppBook]] shouldBe Seq(storedBook)
      }
    }

    "books by pages number greater than and authors with pages number less than" in {
      val greaterThan = 100
      val lessThan = 50

      val service = mock[BookService]

      val router = new BookRouter(service)

      (service.booksByPagesNumberGreaterThanAndAuthorsWithPagesNumberLessThan _)
        .expects(greaterThan, lessThan)
        .returns(Future.successful(Seq(storedBook)))

      Get(s"/books?pagesNumberFrom=$greaterThan&otherAuthorBookPagesNumberTo=$lessThan") ~> router.route ~> check {
        handled shouldBe true
        status shouldBe StatusCodes.OK
        responseAs[Seq[AppBook]] shouldBe Seq(storedBook)
      }
    }
  }
}
