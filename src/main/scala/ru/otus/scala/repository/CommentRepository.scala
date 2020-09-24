package ru.otus.scala.repository

import ru.otus.scala.model.domain.BookComment
import ru.otus.scala.model.domain.author.Author

import scala.concurrent.Future

trait CommentRepository {
  def create(comment: BookComment): Future[BookComment]

  def findAuthorsCommentedMoreThan(times: Int): Seq[Author]

  def deleteAll(): Future[Int]
}
