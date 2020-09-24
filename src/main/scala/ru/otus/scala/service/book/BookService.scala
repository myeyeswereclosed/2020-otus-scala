package ru.otus.scala.service.book

import ru.otus.scala.model.AddBookAuthor.{AddAuthorRequest, AddAuthorResponse}
import ru.otus.scala.model.AllBooks.{AllBooksRequest, AllBooksResponse}
import ru.otus.scala.model.BooksByAuthorLastName.{BooksByAuthorLastNameRequest, BooksByAuthorLastNameResponse}
import ru.otus.scala.model.CreateBook.{BookCreated, CreateBookRequest}
import ru.otus.scala.model.DeleteBook.{DeleteBookRequest, DeleteBookResponse}
import ru.otus.scala.model.GetBook.{GetBookRequest, GetBookResponse}
import ru.otus.scala.model.UpdateBook.{UpdateBookRequest, UpdateBookResponse}
import ru.otus.scala.model.AddBookAuthor.{AddAuthorRequest, AddAuthorResponse}
import ru.otus.scala.model.AllBooks.{AllBooksRequest, AllBooksResponse}
import ru.otus.scala.model.BooksByAuthorLastName.{BooksByAuthorLastNameRequest, BooksByAuthorLastNameResponse}
import ru.otus.scala.model.CreateBook.{BookCreated, CreateBookRequest}
import ru.otus.scala.model.DeleteBook.{DeleteBookRequest, DeleteBookResponse}
import ru.otus.scala.model.GetBook.{GetBookRequest, GetBookResponse}
import ru.otus.scala.model.UpdateBook.{UpdateBookRequest, UpdateBookResponse}
import ru.otus.scala.model.domain.AppBook

import scala.concurrent.Future

trait BookService {
  def create(request: CreateBookRequest): Future[BookCreated]

  def getAll(request: AllBooksRequest): Future[AllBooksResponse]

  def get(request: GetBookRequest): Future[GetBookResponse]

  def update(request: UpdateBookRequest): Future[UpdateBookResponse]

  def delete(request: DeleteBookRequest): Future[DeleteBookResponse]

  def addAuthor(request: AddAuthorRequest): Future[AddAuthorResponse]

  def getAllByAuthorLastName(request: BooksByAuthorLastNameRequest): Future[BooksByAuthorLastNameResponse]

  def booksByPagesNumberGreaterThanAndAuthorsWithPagesNumberLessThan(
    pagesNumberFrom: Int,
    otherAuthorBookPagesNumberTo: Int
  ): Future[Seq[AppBook]]
}
