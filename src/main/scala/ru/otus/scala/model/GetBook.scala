package ru.otus.scala.model

import java.util.UUID

import ru.otus.scala.model.domain.AppBook

object GetBook {
  case class GetBookRequest(id: UUID)

  sealed trait GetBookResponse
  object GetBookResponse {
    case class BookFound(book: AppBook) extends GetBookResponse
    case class BookNotFound(id: UUID) extends GetBookResponse
  }
}
