package ru.otus.scala.repository.dao

import java.util.UUID

import ru.otus.scala.model.domain.BookComment

trait CommentDao[F[_]] {
  def create(comment: BookComment): F[UUID]

  def deleteAll(): F[Int]
}
