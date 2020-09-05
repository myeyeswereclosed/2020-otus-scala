package ru.otus.scala.book.service.comment

import ru.otus.scala.book.model.CreateComment.{CreateCommentRequest, CreateCommentResponse}

trait CommentService {
  def create(request: CreateCommentRequest): CreateCommentResponse
}
