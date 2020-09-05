package ru.otus.scala.book.dao.comment

import ru.otus.scala.book.model.domain.Author
import ru.otus.scala.book.model.domain.Comment.BookComment

trait CommentDao {
  def create(comment: BookComment): BookComment

  def findAuthorsCommentedMoreThan(times: Int): Seq[Author]
}
