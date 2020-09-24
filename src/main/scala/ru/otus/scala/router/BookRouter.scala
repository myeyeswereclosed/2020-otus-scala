package ru.otus.scala.router

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import play.api.libs.json.{Json, OFormat}
import ru.otus.scala.model.AddBookAuthor.AddAuthorResponse.AuthorAdded
import ru.otus.scala.model.AddBookAuthor.{AddAuthorRequest, AddAuthorResponse}
import ru.otus.scala.model.AllBooks.AllBooksRequest
import ru.otus.scala.model.BooksByAuthorLastName.BooksByAuthorLastNameRequest
import ru.otus.scala.model.CreateBook.CreateBookRequest
import ru.otus.scala.model.DeleteBook.DeleteBookResponse.BookDeleted
import ru.otus.scala.model.DeleteBook.{DeleteBookRequest, DeleteBookResponse}
import ru.otus.scala.model.GetBook.GetBookRequest
import ru.otus.scala.model.GetBook.GetBookResponse.BookFound
import ru.otus.scala.model.UpdateBook.UpdateBookRequest
import ru.otus.scala.model.UpdateBook.UpdateBookResponse.{BookNotFound, BookUpdated, CantUpdateBookWithoutId}
import ru.otus.scala.model.AddBookAuthor.AddAuthorResponse
import ru.otus.scala.model.DeleteBook.{DeleteBookRequest, DeleteBookResponse}
import ru.otus.scala.model.GetBook.GetBookRequest
import ru.otus.scala.model.UpdateBook.UpdateBookResponse.CantUpdateBookWithoutId
import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.model.domain.author.Author
import ru.otus.scala.route.Router
import ru.otus.scala.service.book.BookService

class BookRouter(service: BookService) extends Router {
  implicit lazy val authorFormat: OFormat[Author] = Json.format
  implicit lazy val bookFormat: OFormat[AppBook] = Json.format

  def route: Route =
    pathPrefix("book") { createBook ~ getBook ~ updateBook ~ deleteBook ~ addAuthor } ~
    pathPrefix("books") {
      booksByAuthorLastName ~ booksByPagesNumberGreaterThanAndAuthorsWithPagesNumberLessThan ~ allBooks
    }

  private def allBooks: Route =
    (get & parameters(
      "page".as[Int].?(default = 0),
      "size".as[Int].?(default = 100)
    )) {
      (page: Int, size: Int) =>
        onSuccess(service.getAll(AllBooksRequest(page, size))) {
          response => complete(response.books)
        }
    }

  private def createBook: Route =
    (post & entity(as[AppBook])) {
      book => onSuccess(service.create(CreateBookRequest(book))) {
        response => complete(response.book)
      }
    }

  private def getBook: Route =
    (get & path(JavaUUID.map(GetBookRequest))) {
      request =>
        onSuccess(service.get(request)) {
          case BookFound(book) => complete(book)
          case _ => complete(StatusCodes.NotFound)
        }
    }

  private def updateBook: Route =
    (put & entity(as[AppBook])) {
      book =>
        onSuccess(service.update(UpdateBookRequest(book))) {
          case BookUpdated(book) => complete(book)
          case BookNotFound(_) => complete(StatusCodes.NotFound)
          case CantUpdateBookWithoutId => complete(StatusCodes.NotFound)
        }
    }

  private def deleteBook: Route =
    (delete & path(JavaUUID.map(DeleteBookRequest))) {
      request =>
        onSuccess(service.delete(request)) {
          case BookDeleted(id) => complete(id)
          case DeleteBookResponse.BookNotFound(_) => complete(StatusCodes.NotFound)
        }
    }

  private def addAuthor: Route =
    (post & path(JavaUUID / "author") & entity(as[Author])) {
      (id, author) =>
        onSuccess(service.addAuthor(AddAuthorRequest(id, author))) {
          case AuthorAdded(book) => complete(book)
          case AddAuthorResponse.BookNotFound(_) => complete(StatusCodes.NotFound)
        }
    }

  private def booksByAuthorLastName: Route =
    (get & parameter("authorLastName".as[String])) {
      lastName =>
        onSuccess(service.getAllByAuthorLastName(BooksByAuthorLastNameRequest(lastName))) {
          response => complete(response.books)
        }
    }

  private def booksByPagesNumberGreaterThanAndAuthorsWithPagesNumberLessThan: Route =
    (get & parameters(
      "pagesNumberFrom".as[Int],
      "otherAuthorBookPagesNumberTo".as[Int]
    )) {
      (pagesFrom, otherAuthorsBookPagesTo) =>
        complete(
          service
            .booksByPagesNumberGreaterThanAndAuthorsWithPagesNumberLessThan(
              pagesFrom,
              otherAuthorsBookPagesTo
            )
        )
    }
}
