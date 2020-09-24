package ru.otus.scala.model

import ru.otus.scala.model.domain.AppBook

object AllBooks {
  case class AllBooksRequest(page: Int, size: Int)

  case class AllBooksResponse(books: Seq[AppBook])
}
