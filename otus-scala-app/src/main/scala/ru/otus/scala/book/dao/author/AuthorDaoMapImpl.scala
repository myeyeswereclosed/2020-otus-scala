package ru.otus.scala.book.dao.author

import java.util.UUID

import ru.otus.scala.book.model.domain.Author

class AuthorDaoMapImpl extends AuthorDao {
  private var authors: Map[UUID, Author] = Map.empty

  def create(author: Author): Author = {
    val id = UUID.randomUUID()
    val newAuthor = author.copy(id = Some(id))

    authors += (id -> newAuthor)

    newAuthor
  }

  def findByFirstAndLastName(firstName: String, lastName: String): Option[Author] = {
    authors.values.find(author => author.hasFirstAndLastName(firstName, lastName))
  }

  def addAll(authors: Seq[Author]): Seq[Author] =
    authors
      .filter(author => findByFirstAndLastName(author.firstName, author.lastName).isEmpty)
      .map(create)
}
