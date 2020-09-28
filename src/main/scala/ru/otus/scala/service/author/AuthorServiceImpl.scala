package ru.otus.scala.service.author

import ru.otus.scala.model.AuthorsByYearOfPublishing.{AuthorsByYearOfPublishingRequest, AuthorsByYearOfPublishingResponse}
import ru.otus.scala.model.domain.AppAuthor
import AppAuthor.Author
import ru.otus.scala.repository.{AuthorRepository, BookRepository, CommentRepository}

import scala.concurrent.{ExecutionContext, Future}

class AuthorServiceImpl(
  authorRepository: AuthorRepository,
  commentRepository: CommentRepository
)(implicit ec: ExecutionContext) extends AuthorService {

  def getAllPublishedIn(request: AuthorsByYearOfPublishingRequest): Future[AuthorsByYearOfPublishingResponse] =
    authorRepository.findPublishedIn(request.year).map(AuthorsByYearOfPublishingResponse)

  def getAllCommentedMoreThan(commentsNumber: Int): Seq[Author] =
    commentRepository.findAuthorsCommentedMoreThan(commentsNumber)
}
