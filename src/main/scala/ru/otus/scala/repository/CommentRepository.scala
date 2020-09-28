package ru.otus.scala.repository

import ru.otus.scala.model.domain.BookComment.BookComment

import scala.concurrent.Future

trait CommentRepository {
  def create(comment: BookComment): Future[BookComment]

  def deleteAll(): Future[Int]
}
