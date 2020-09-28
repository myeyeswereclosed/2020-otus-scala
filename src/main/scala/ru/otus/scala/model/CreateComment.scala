package ru.otus.scala.model

import java.util.UUID

import ru.otus.scala.model.domain.BookComment
import ru.otus.scala.model.domain.BookComment.BookComment

object CreateComment {
  case class CreateCommentRequest(bookId: UUID, text: String)

  sealed trait CreateCommentResponse

  case class CommentCreated(comment: BookComment) extends CreateCommentResponse
  case class BookNotFound(bookId: UUID) extends CreateCommentResponse
}
