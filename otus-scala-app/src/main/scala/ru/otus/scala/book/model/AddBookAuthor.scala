package ru.otus.scala.book.model

import java.util.UUID

import ru.otus.scala.book.model.domain.{Author, Book}

object AddBookAuthor {
  case class AddAuthorRequest(bookId: UUID, author: Author)

  sealed trait AddAuthorResponse

  object AddAuthorResponse {
    case class AuthorAdded(book: Book) extends AddAuthorResponse
    case class BookNotFound(id: UUID) extends AddAuthorResponse
  }
}
