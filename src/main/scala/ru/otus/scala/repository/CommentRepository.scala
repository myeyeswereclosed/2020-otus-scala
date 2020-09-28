package ru.otus.scala.repository

import ru.otus.scala.model.domain.BookComment
import ru.otus.scala.model.domain.AppAuthor.Author
import ru.otus.scala.model.domain.BookComment.BookComment

import scala.concurrent.Future

trait CommentRepository {
  def create(comment: BookComment): Future[BookComment]

  def findAuthorsCommentedMoreThan(times: Int): Seq[Author]

  def deleteAll(): Future[Int]
}
