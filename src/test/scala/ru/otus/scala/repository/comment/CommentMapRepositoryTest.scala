package ru.otus.scala.repository.comment

import ru.otus.scala.repository.{BookRepository, CommentRepository}
import ru.otus.scala.repository.impl.map.CommentMapRepository

class CommentMapRepositoryTest extends CommentRepositoryTest("CommentMapRepositoryTest") {
  def createRepository(): CommentRepository = new CommentMapRepository

  def createBookRepository(): Option[BookRepository] = None
}
