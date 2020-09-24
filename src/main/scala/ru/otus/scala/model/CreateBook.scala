package ru.otus.scala.model

import ru.otus.scala.model.domain.AppBook

object CreateBook {
  case class CreateBookRequest(book: AppBook)

  case class BookCreated(book: AppBook)
}
