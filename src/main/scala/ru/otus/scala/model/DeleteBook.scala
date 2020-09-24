package ru.otus.scala.model

import java.util.UUID

object DeleteBook {

  case class DeleteBookRequest(id: UUID)

  sealed trait DeleteBookResponse

  object DeleteBookResponse {
    case class BookDeleted(id: UUID) extends DeleteBookResponse
    case class BookNotFound(id: UUID) extends DeleteBookResponse
  }

}
