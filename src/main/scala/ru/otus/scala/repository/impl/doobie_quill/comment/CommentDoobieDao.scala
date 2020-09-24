package ru.otus.scala.repository.impl.doobie_quill.comment

import java.util.UUID

import doobie.free.connection.ConnectionIO
import ru.otus.scala.model.domain.BookComment
import ru.otus.scala.repository.dao.CommentDao

trait CommentDoobieDao extends CommentDao[ConnectionIO] {
  def create(comment: BookComment): ConnectionIO[UUID]

  def deleteAll(): ConnectionIO[Int]
}
