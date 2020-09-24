package ru.otus.scala.service.author

import ru.otus.scala.model.AuthorsByYearOfPublishing.{AuthorsByYearOfPublishingRequest, AuthorsByYearOfPublishingResponse}
import ru.otus.scala.model.domain.author.Author

import scala.concurrent.Future

trait AuthorService {
  def getAllPublishedIn(request: AuthorsByYearOfPublishingRequest): Future[AuthorsByYearOfPublishingResponse]

  def getAllCommentedMoreThan(commentsNumber: Int): Seq[Author]
}
