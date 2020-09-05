package ru.otus.scala.book.model.domain

import java.time.ZonedDateTime
import java.util.UUID

object Comment {

  case class CommentText(text: String)

  case class BookComment(id: Option[UUID], text: String, book: Book, madeAt: ZonedDateTime = ZonedDateTime.now())

}