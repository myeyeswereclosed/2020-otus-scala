package ru.otus.scala.repository.impl.doobie_quill.comment.dao

import java.util.UUID

import doobie.ConnectionIO
import doobie.quill.DoobieContext
import io.getquill.SnakeCase
import ru.otus.scala.model.domain.BookComment
import ru.otus.scala.model.domain.BookComment.BookComment
import ru.otus.scala.repository.impl
import ru.otus.scala.repository.impl.Comment
import ru.otus.scala.repository.impl.doobie_quill.comment.CommentDoobieDao

class CommentQuillDao extends CommentDoobieDao {
  val dc = new DoobieContext.Postgres(SnakeCase)
  import dc._

  private val table = quote { querySchema[Comment]("comment") }

  def create(comment: BookComment): ConnectionIO[UUID] =
    run {
      quote {
        table
          .insert(lift(impl.Comment(UUID.randomUUID(), comment.text, comment.book.id.get, comment.madeAt)))
          .returningGenerated(_.id)
      }
    }

  def deleteAll(): ConnectionIO[Int] =
    run {
      quote {
        table.delete
      }
    }.map(_.intValue)
}
