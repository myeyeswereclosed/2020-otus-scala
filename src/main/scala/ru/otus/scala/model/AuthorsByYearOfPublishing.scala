package ru.otus.scala.model

import ru.otus.scala.model.domain.author.Author

object AuthorsByYearOfPublishing {
  case class AuthorsByYearOfPublishingRequest(year: Int)

  case class AuthorsByYearOfPublishingResponse(authors: Seq[Author])
}
