package ru.otus.scala.repository

import ru.otus.scala.model.domain.AppAuthor.Author

import scala.concurrent.Future

trait AuthorRepository {
  def create(author: Author): Future[Author]

  def findByFirstAndLastName(firstName: String, lastName: String): Future[Option[Author]]

  def findPublishedIn(year: Int): Future[Seq[Author]]

  def findAuthorsWithBooksPagesLessThan(pages: Int, amongAuthors: Set[Author]): Future[Seq[Author]]

  def deleteAll(): Future[Int]
}
