package ru.otus.scala.repository

import ru.otus.scala.model.domain.author.Author

import scala.concurrent.Future

trait AuthorRepository {
  def create(author: Author): Future[Author]

  def findByFirstAndLastName(firstName: String, lastName: String): Future[Option[Author]]

  def deleteAll(): Future[Int]
}
