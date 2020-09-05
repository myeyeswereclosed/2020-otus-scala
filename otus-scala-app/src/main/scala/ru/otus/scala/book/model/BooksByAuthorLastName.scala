package ru.otus.scala.book.model

import ru.otus.scala.book.model.domain.Book

object BooksByAuthorLastName {
  case class BooksByAuthorLastNameRequest(lastName: String)

  case class BooksByAuthorLastNameResponse(books: Seq[Book])
}
