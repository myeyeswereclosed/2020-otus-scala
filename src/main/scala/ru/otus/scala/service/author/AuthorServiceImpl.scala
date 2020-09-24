package ru.otus.scala.service.author

import ru.otus.scala.model.AuthorsByYearOfPublishing.{AuthorsByYearOfPublishingRequest, AuthorsByYearOfPublishingResponse}
import ru.otus.scala.model.domain.author.Author
import ru.otus.scala.repository.{BookRepository, CommentRepository}

import scala.concurrent.{ExecutionContext, Future}

class AuthorServiceImpl(
  bookRepository: BookRepository,
  commentRepository: CommentRepository
)(implicit ec: ExecutionContext) extends AuthorService {

  def getAllPublishedIn(request: AuthorsByYearOfPublishingRequest): Future[AuthorsByYearOfPublishingResponse] =
    bookRepository.findAuthorsPublishedIn(request.year).map(AuthorsByYearOfPublishingResponse)

  def getAllCommentedMoreThan(commentsNumber: Int): Seq[Author] =
    commentRepository.findAuthorsCommentedMoreThan(commentsNumber)
}
