package ru.otus.scala.book.dao.book

import java.util.UUID

import ru.otus.scala.book.model.domain.{Author, Book}

class BookDaoMapImpl extends BookDao {
  private var books: Map[UUID, Book] = Map.empty

  def create(book: Book): Book = {
    val id = UUID.randomUUID()
    val newBook = book.copy(id = Some(id))

    books += (id -> newBook)

    newBook
  }

  def findAll(page: Int, size: Int): Seq[Book] = books.values.slice(page * size, page * size + size).toSeq

  def findById(id: UUID): Option[Book] = books.get(id)

  def update(book: Book): Option[Book] =
    for {
      id <- book.id
      _  <- books.get(id)
    } yield {
      books += (id -> book)
      book
    }

  def delete(id: UUID): Option[Book] =
    books.get(id) match {
      case Some(user) =>
        books -= id
        Some(user)
      case None => None
    }

  def findAllByAuthorLastName(lastName: String): Seq[Book] =
    books.values.filter(_.authors.exists(_.hasLastName(lastName))).toSeq

  def findAuthorsPublishedIn(year: Int): Seq[Author] =
    books
      .values
      .filter(_.isPublishedIn(year))
      .flatMap(_.authors)
      .toSeq
      .distinct

  def findAllWithPagesNumberGreaterThan(pages: Int): Seq[Book] =
    books
      .values
      .filter(_.pagesNumber > pages)
      .toSeq

  def findAuthorsWithBooksPagesLessThan(pages: Int, amongAuthors: Seq[Author]): Seq[Author] =
    books
      .values
      .filter(book => book.pagesNumber < pages && amongAuthors.intersect(book.authors).nonEmpty)
      .flatMap(_.authors)
      .toSeq
      .distinct
}
