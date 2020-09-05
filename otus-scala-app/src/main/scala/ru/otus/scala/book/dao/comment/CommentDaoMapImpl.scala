package ru.otus.scala.book.dao.comment
import java.util.UUID

import ru.otus.scala.book.model.domain.Author
import ru.otus.scala.book.model.domain.Comment.BookComment

class CommentDaoMapImpl extends CommentDao {
  private var comments: Map[UUID, BookComment] = Map.empty

  def create(comment: BookComment): BookComment = {
    val id = UUID.randomUUID()
    val newComment = comment.copy(id = Some(id))

    comments += (id -> newComment)

    newComment
  }

  def findAuthorsCommentedMoreThan(times: Int): Seq[Author] =
    comments
      .values
      .groupBy(_.book)
      .filter {
        case (_, comments) => comments.size > times
      }
      .keys
      .flatMap(_.authors)
      .toSeq
      .distinct
}
