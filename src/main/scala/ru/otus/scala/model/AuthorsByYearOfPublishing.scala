package ru.otus.scala.model

import ru.otus.scala.model.domain.AppAuthor
import AppAuthor.Author

object AuthorsByYearOfPublishing {
  case class AuthorsByYearOfPublishingRequest(year: Int)

  case class AuthorsByYearOfPublishingResponse(authors: Seq[Author])
}
