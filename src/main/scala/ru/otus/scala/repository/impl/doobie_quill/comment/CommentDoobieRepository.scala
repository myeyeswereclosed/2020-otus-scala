package ru.otus.scala.repository.impl.doobie_quill.comment

import cats.effect.IO
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import ru.otus.scala.model.domain.AppAuthor
import AppAuthor.Author
import ru.otus.scala.model.domain.BookComment.BookComment
import ru.otus.scala.repository.CommentRepository
import ru.otus.scala.repository.dao.CommentDao

import scala.concurrent.Future

class CommentDoobieRepository(
  commentDao: CommentDao[ConnectionIO],
  transactor: Transactor[IO]
) extends CommentRepository {
  def create(comment: BookComment): Future[BookComment] =
    commentDao
      .create(comment)
      .transact(transactor)
      .map(id => comment.copy(id = Some(id)))
      .unsafeToFuture()

  def findAuthorsCommentedMoreThan(times: Int): Seq[Author] = ???

  def deleteAll(): Future[Int] =
    commentDao.deleteAll().transact(transactor).unsafeToFuture()
}
