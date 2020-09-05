package ru.otus.scala.book.service.author

import ru.otus.scala.book.model.AuthorsByYearOfPublishing.{AuthorsByYearOfPublishingRequest, AuthorsByYearOfPublishingResponse}
import ru.otus.scala.book.model.domain.Author

trait AuthorService {
  def getAllPublishedIn(request: AuthorsByYearOfPublishingRequest): AuthorsByYearOfPublishingResponse

  def getAllCommentedMoreThan(commentsNumber: Int): Seq[Author]
}
