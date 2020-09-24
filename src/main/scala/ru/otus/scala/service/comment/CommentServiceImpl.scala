package ru.otus.scala.service.comment

import ru.otus.scala.model.CreateComment.{BookNotFound, CommentCreated, CreateCommentRequest, CreateCommentResponse}
import ru.otus.scala.model.domain.BookComment
import ru.otus.scala.repository.{BookRepository, CommentRepository}

import scala.concurrent.{ExecutionContext, Future}

class CommentServiceImpl(
  bookRepository: BookRepository,
  commentRepository: CommentRepository
)(implicit ec: ExecutionContext) extends CommentService {
  def create(request: CreateCommentRequest): Future[CreateCommentResponse] =
    for {
      maybeBook <- bookRepository.findById(request.bookId)
      response <-
        maybeBook
          .map(
            book =>
              commentRepository
                .create(BookComment(id = None, text = request.text, book = book))
                .map(CommentCreated)
            )
          .getOrElse(Future.successful(BookNotFound(request.bookId)))
    } yield response
}
