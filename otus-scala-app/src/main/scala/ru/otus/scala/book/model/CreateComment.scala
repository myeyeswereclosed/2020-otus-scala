package ru.otus.scala.book.model

import java.util.UUID

import ru.otus.scala.book.model.domain.Comment.BookComment

object CreateComment {
  case class CreateCommentRequest(bookId: UUID, text: String)

  sealed trait CreateCommentResponse

  case class CommentCreated(comment: BookComment) extends CreateCommentResponse
  case class BookNotFound(bookId: UUID) extends CreateCommentResponse
}
