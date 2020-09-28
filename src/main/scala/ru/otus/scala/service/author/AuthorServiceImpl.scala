package ru.otus.scala.service.author

import ru.otus.scala.model.AuthorsByYearOfPublishing.{AuthorsByYearOfPublishingRequest, AuthorsByYearOfPublishingResponse}
import ru.otus.scala.repository.AuthorRepository

import scala.concurrent.{ExecutionContext, Future}

class AuthorServiceImpl(
  authorRepository: AuthorRepository
)(implicit ec: ExecutionContext) extends AuthorService {

  def getAllPublishedIn(request: AuthorsByYearOfPublishingRequest): Future[AuthorsByYearOfPublishingResponse] =
    authorRepository.findPublishedIn(request.year).map(AuthorsByYearOfPublishingResponse)
}
