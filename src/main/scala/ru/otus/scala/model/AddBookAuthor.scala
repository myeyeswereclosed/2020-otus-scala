package ru.otus.scala.model

import java.util.UUID

import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.model.domain.AppAuthor.Author

object AddBookAuthor {
  case class AddAuthorRequest(bookId: UUID, author: Author)

  sealed trait AddAuthorResponse

  object AddAuthorResponse {
    case class AuthorAdded(book: AppBook) extends AddAuthorResponse
    case class BookNotFound(id: UUID) extends AddAuthorResponse
  }
}
