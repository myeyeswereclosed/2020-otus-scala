package ru.otus.scala.repository.impl

import java.util.UUID

import ru.otus.scala.model.domain.AppAuthor.Author

case class AuthorModel(id: UUID, firstName: String, lastName: String) {
  val toAuthor: Author = Author(Some(id), firstName, lastName)
}

object AuthorModel {
  def fromAuthor(author: Author): AuthorModel =
    AuthorModel(author.id.getOrElse(UUID.randomUUID()), author.firstName, author.lastName)
}
