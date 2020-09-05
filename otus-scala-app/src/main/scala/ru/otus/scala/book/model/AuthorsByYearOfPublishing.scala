package ru.otus.scala.book.model

import ru.otus.scala.book.model.domain.Author

object AuthorsByYearOfPublishing {
  case class AuthorsByYearOfPublishingRequest(year: Int)

  case class AuthorsByYearOfPublishingResponse(authors: Seq[Author])
}
