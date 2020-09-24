package ru.otus.scala.repository

import java.util.UUID

import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.model.domain.author.Author

import scala.concurrent.Future

trait BookRepository {
  def create(book: AppBook): Future[AppBook]

  def findAll(page: Int, size: Int): Future[Seq[AppBook]]

  def findById(id: UUID): Future[Option[AppBook]]

  def update(book: AppBook): Future[Option[AppBook]]

  def delete(id: UUID): Future[Option[UUID]]

  def findAllByAuthorLastName(lastName: String): Future[Seq[AppBook]]

  def findAuthorsPublishedIn(year: Int): Future[Seq[Author]]

  def findAllWithPagesNumberGreaterThan(pages: Int): Future[Seq[AppBook]]

  def findAuthorsWithBooksPagesLessThan(pages: Int, amongAuthors: Set[Author]): Future[Seq[Author]]

  def deleteAllWithAuthors(): Future[Int]
}
