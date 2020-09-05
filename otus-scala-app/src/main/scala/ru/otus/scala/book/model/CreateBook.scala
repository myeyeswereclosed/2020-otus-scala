package ru.otus.scala.book.model

import ru.otus.scala.book.model.domain.Book

object CreateBook {
  case class CreateBookRequest(book: Book)

  case class BookCreated(book: Book)
}
