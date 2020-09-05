package ru.otus.scala.book.router

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
import play.api.libs.json.{Json, OFormat}
import ru.otus.scala.book.model.AddBookAuthor.AddAuthorResponse.AuthorAdded
import ru.otus.scala.book.model.AddBookAuthor.{AddAuthorRequest, AddAuthorResponse}
import ru.otus.scala.book.model.AllBooks.AllBooksRequest
import ru.otus.scala.book.model.BooksByAuthorLastName.BooksByAuthorLastNameRequest
import ru.otus.scala.book.model.CreateBook.CreateBookRequest
import ru.otus.scala.book.model.DeleteBook.DeleteBookResponse.BookDeleted
import ru.otus.scala.book.model.DeleteBook.{DeleteBookRequest, DeleteBookResponse}
import ru.otus.scala.book.model.GetBook.GetBookRequest
import ru.otus.scala.book.model.GetBook.GetBookResponse.BookFound
import ru.otus.scala.book.model.UpdateBook.UpdateBookRequest
import ru.otus.scala.book.model.UpdateBook.UpdateBookResponse.{BookNotFound, BookUpdated, CantUpdateBookWithoutId}
import ru.otus.scala.book.model.domain.{Author, Book}
import ru.otus.scala.book.service.book.BookService
import ru.otus.scala.route.Router

class BookRouter(service: BookService) extends Router {
  implicit lazy val authorFormat: OFormat[Author] = Json.format
  implicit lazy val bookFormat: OFormat[Book] = Json.format

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
        val response = service.getAll(AllBooksRequest(page, size))
        complete(response.books)
    }

  private def createBook: Route =
    (post & entity(as[Book])) {
      book => complete(service.create(CreateBookRequest(book)).book)
    }

  private def getBook: Route =
    (get & path(JavaUUID.map(GetBookRequest))) {
      request =>
        service.get(request) match {
          case BookFound(book) => complete(book)
          case _ => complete(StatusCodes.NotFound)
        }
    }

  private def updateBook: Route =
    (put & entity(as[Book])) {
      book =>
        service.update(UpdateBookRequest(book)) match {
          case BookUpdated(book) => complete(book)
          case BookNotFound(_) => complete(StatusCodes.NotFound)
          case CantUpdateBookWithoutId => complete(StatusCodes.NotFound)
        }
    }

  private def deleteBook: Route =
    (delete & path(JavaUUID.map(DeleteBookRequest))) {
      request =>
        service.delete(request) match {
          case BookDeleted(book) => complete(book)
          case DeleteBookResponse.BookNotFound(_) => complete(StatusCodes.NotFound)
        }
    }

  private def addAuthor: Route =
    (post & path(JavaUUID / "author") & entity(as[Author])) {
      (id, author) =>
        service.addAuthor(AddAuthorRequest(id, author)) match {
          case AuthorAdded(book) => complete(book)
          case AddAuthorResponse.BookNotFound(_) => complete(StatusCodes.NotFound)
        }
    }

  private def booksByAuthorLastName: Route =
    (get & parameter("authorLastName".as[String])) {
      lastName => complete(service.getAllByAuthorLastName(BooksByAuthorLastNameRequest(lastName)).books)
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
