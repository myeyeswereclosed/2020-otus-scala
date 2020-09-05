package ru.otus.scala.book.dao.author

import ru.otus.scala.book.model.domain.Author

trait AuthorDao {
  def create(author: Author): Author

  def addAll(authors: Seq[Author]): Seq[Author]

  def findByFirstAndLastName(firstName: String, lastName: String): Option[Author]
}
