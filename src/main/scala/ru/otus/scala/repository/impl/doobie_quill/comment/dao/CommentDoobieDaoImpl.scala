package ru.otus.scala.repository.impl.doobie_quill.comment.dao

import java.util.UUID

import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.implicits.javatime._
import doobie.postgres.implicits._
import ru.otus.scala.model.domain.BookComment.BookComment
import ru.otus.scala.repository.impl.doobie_quill.comment.CommentDoobieDao

class CommentDoobieDaoImpl extends CommentDoobieDao {
  def create(comment: BookComment): ConnectionIO[UUID] =
    sql"""insert into comment(text, book_id, made_at) values(${comment.text}, ${comment.book.id}, ${comment.madeAt})"""
      .update
      .withGeneratedKeys[UUID]("id")
      .compile
      .lastOrError

  def deleteAll(): ConnectionIO[Int] =
    sql"""delete from comment""".update.run
}
