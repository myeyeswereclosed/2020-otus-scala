package ru.otus.scala.repository.impl

import java.time.LocalDateTime
import java.util.UUID

case class Comment(id: UUID, text: String, bookId: UUID, madeAt: LocalDateTime)
