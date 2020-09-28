package ru.otus.scala.repository.impl.map

import java.util.UUID

import ru.otus.scala.model.domain.{AppAuthor, BookComment}
import AppAuthor.Author
import ru.otus.scala.model.domain.BookComment.BookComment
import ru.otus.scala.repository.CommentRepository

import scala.concurrent.Future

class CommentMapRepository extends CommentRepository {
  private var comments: Map[UUID, BookComment] = Map.empty

  def create(comment: BookComment): Future[BookComment] = {
    val id = UUID.randomUUID()
    val newComment = comment.copy(id = Some(id))

    comments += (id -> newComment)

    Future.successful(newComment)
  }

  def deleteAll(): Future[Int] = {
    val size = comments.size

    comments = Map.empty

    Future.successful(size)
  }
}
