package ru.otus.scala.repository.impl.slick.comment

import ru.otus.scala.model.domain.BookComment
import ru.otus.scala.model.domain.author.Author
import ru.otus.scala.repository.CommentRepository
import ru.otus.scala.repository.dao.CommentDao
import ru.otus.scala.repository.impl.slick.DbAction.DbAction
import slick.jdbc.JdbcBackend.Database
import cats.syntax.option._

import scala.concurrent.{ExecutionContext, Future}

class CommentSlickRepository(
  dao: CommentDao[DbAction],
  db: Database
)(implicit ec: ExecutionContext) extends CommentRepository {
  def create(comment: BookComment): Future[BookComment] =
    db.run(dao.create(comment)).map(id => comment.copy(id = id.some))

  def findAuthorsCommentedMoreThan(times: Int): Seq[Author] = ???

  def deleteAll(): Future[Int] = db.run(dao.deleteAll())
}
