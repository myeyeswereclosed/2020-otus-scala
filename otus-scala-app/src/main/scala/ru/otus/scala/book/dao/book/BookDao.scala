package ru.otus.scala.book.dao.book

import java.util.UUID

import ru.otus.scala.book.model.domain.{Author, Book}

trait BookDao {
  def create(book: Book): Book

  def findAll(page: Int, size: Int): Seq[Book]

  def findById(id: UUID): Option[Book]

  def update(book: Book): Option[Book]

  def delete(id: UUID): Option[Book]

  def findAllByAuthorLastName(lastName: String): Seq[Book]

  def findAuthorsPublishedIn(year: Int): Seq[Author]

  def findAllWithPagesNumberGreaterThan(pages: Int): Seq[Book]

  def findAuthorsWithBooksPagesLessThan(pages: Int, amongAuthors: Seq[Author]): Seq[Author]
}
