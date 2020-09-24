package ru.otus.scala.repository.impl.map

import java.util.UUID

import ru.otus.scala.model.domain.author.Author
import ru.otus.scala.repository.AuthorRepository

import scala.concurrent.Future

class AuthorMapRepository extends AuthorRepository {
  private var authors: Map[UUID, Author] = Map.empty

  def create(author: Author): Future[Author] = {
    val id = UUID.randomUUID()
    val newAuthor = author.copy(id = Some(id))

    authors += (id -> newAuthor)

    Future.successful(newAuthor)
  }

  def findByFirstAndLastName(firstName: String, lastName: String): Future[Option[Author]] = {
    Future.successful(authors.values.find(author => author.hasFirstAndLastName(firstName, lastName)))
  }

  def deleteAll(): Future[Int] = {
    val size = authors.size

    authors = Map.empty

    Future.successful(size)
  }
}
