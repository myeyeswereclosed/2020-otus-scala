package ru.otus.scala.book.service.book

import ru.otus.scala.book.model.AddBookAuthor.{AddAuthorRequest, AddAuthorResponse}
import ru.otus.scala.book.model.AllBooks.{AllBooksRequest, AllBooksResponse}
import ru.otus.scala.book.model.BooksByAuthorLastName.{BooksByAuthorLastNameRequest, BooksByAuthorLastNameResponse}
import ru.otus.scala.book.model.CreateBook.{BookCreated, CreateBookRequest}
import ru.otus.scala.book.model.DeleteBook.{DeleteBookRequest, DeleteBookResponse}
import ru.otus.scala.book.model.GetBook.{GetBookRequest, GetBookResponse}
import ru.otus.scala.book.model.UpdateBook.{UpdateBookRequest, UpdateBookResponse}
import ru.otus.scala.book.model.domain.Book

trait BookService {
  def create(request: CreateBookRequest): BookCreated

  def getAll(request: AllBooksRequest): AllBooksResponse

  def get(request: GetBookRequest): GetBookResponse

  def update(request: UpdateBookRequest): UpdateBookResponse

  def delete(request: DeleteBookRequest): DeleteBookResponse

  def addAuthor(request: AddAuthorRequest): AddAuthorResponse

  def getAllByAuthorLastName(request: BooksByAuthorLastNameRequest): BooksByAuthorLastNameResponse

  def booksByPagesNumberGreaterThanAndAuthorsWithPagesNumberLessThan(
    pagesNumberFrom: Int,
    otherAuthorBookPagesNumberTo: Int
  ): Seq[Book]
}
