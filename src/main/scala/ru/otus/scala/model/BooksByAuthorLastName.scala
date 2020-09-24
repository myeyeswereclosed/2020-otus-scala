package ru.otus.scala.model

import ru.otus.scala.model.domain.AppBook

object BooksByAuthorLastName {
  case class BooksByAuthorLastNameRequest(lastName: String)

  case class BooksByAuthorLastNameResponse(books: Seq[AppBook])
}
