package ru.otus.scala.model.domain

import java.time.LocalDateTime
import java.util.UUID

object BookComment {

  case class CommentText(text: String)

  case class BookComment(id: Option[UUID], text: String, book: AppBook, madeAt: LocalDateTime = LocalDateTime.now())

}
