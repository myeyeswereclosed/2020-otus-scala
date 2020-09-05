package ru.otus.scala.book.model

import java.util.UUID

import ru.otus.scala.book.model.domain.Book

object DeleteBook {

  case class DeleteBookRequest(id: UUID)

  sealed trait DeleteBookResponse

  object DeleteBookResponse {
    case class BookDeleted(book: Book) extends DeleteBookResponse
    case class BookNotFound(id: UUID) extends DeleteBookResponse
  }

}
