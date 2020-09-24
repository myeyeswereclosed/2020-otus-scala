package ru.otus.scala.service.comment

import ru.otus.scala.model.CreateComment.{CreateCommentRequest, CreateCommentResponse}

import scala.concurrent.Future

trait CommentService {
  def create(request: CreateCommentRequest): Future[CreateCommentResponse]
}
