package ru.otus.scala.book.model

import java.util.UUID

import ru.otus.scala.book.model.domain.Book

object UpdateBook {

  case class UpdateBookRequest(book: Book)

  sealed trait UpdateBookResponse

  object UpdateBookResponse {
    final case class BookUpdated(book: Book) extends UpdateBookResponse
    final case class BookNotFound(id: UUID) extends UpdateBookResponse
    final case object CantUpdateBookWithoutId extends UpdateBookResponse
  }

}
