package ru.otus.scala.book.model

import ru.otus.scala.book.model.domain.Book

object AllBooks {
  case class AllBooksRequest(page: Int, size: Int)

  case class AllBooksResponse(books: Seq[Book])
}
