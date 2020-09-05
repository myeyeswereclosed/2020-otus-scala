package ru.otus.scala.book.model

import java.util.UUID

import ru.otus.scala.book.model.domain.Book

object GetBook {
  case class GetBookRequest(id: UUID)

  sealed trait GetBookResponse
  object GetBookResponse {
    case class BookFound(book: Book) extends GetBookResponse
    case class BookNotFound(id: UUID) extends GetBookResponse
  }
}
