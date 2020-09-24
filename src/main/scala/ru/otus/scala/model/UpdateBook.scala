package ru.otus.scala.model

import java.util.UUID

import ru.otus.scala.model.domain.AppBook

object UpdateBook {

  case class UpdateBookRequest(book: AppBook)

  sealed trait UpdateBookResponse

  object UpdateBookResponse {
    final case class BookUpdated(book: AppBook) extends UpdateBookResponse
    final case class BookNotFound(id: UUID) extends UpdateBookResponse
    final case object CantUpdateBookWithoutId extends UpdateBookResponse
  }

}
