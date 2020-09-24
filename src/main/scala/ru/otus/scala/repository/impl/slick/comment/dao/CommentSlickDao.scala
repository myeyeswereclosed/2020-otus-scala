package ru.otus.scala.repository.impl.slick.comment.dao

import java.util.UUID

import ru.otus.scala.model.domain.BookComment
import ru.otus.scala.repository.dao.CommentDao
import ru.otus.scala.repository.impl.Comment
import ru.otus.scala.repository.impl.slick.DbAction.DbAction

import scala.concurrent.ExecutionContext
//import ru.otus.scala.repository.impl.slick.Tables._
import slick.jdbc.PostgresProfile.api._

class CommentSlickDao(implicit ec: ExecutionContext) extends CommentDao[DbAction] {
  import ru.otus.scala.repository.impl.slick.Tables._

  def create(comment: BookComment): DbAction[UUID] =
    (comments returning comments.map(_.id)) +=
      Comment(UUID.randomUUID(), comment.text, comment.book.id.get, comment.madeAt)

  def deleteAll(): DbAction[Int] = comments.delete
}
