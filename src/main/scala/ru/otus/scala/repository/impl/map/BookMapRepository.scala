package ru.otus.scala.repository.impl.map

import java.util.UUID

import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.model.domain.author.Author
import ru.otus.scala.repository.BookRepository

import scala.concurrent.Future

class BookMapRepository extends BookRepository {
  private var books: Map[UUID, AppBook] = Map.empty

  def create(book: AppBook): Future[AppBook] = {
    val id = UUID.randomUUID()

    val authors = book.authors.map(_.copy(id = Some(UUID.randomUUID())))

    val newBook = book.copy(id = Some(id), authors = authors)

    books += (id -> newBook)

    Future.successful(newBook)
  }

  def findAll(page: Int, size: Int): Future[Seq[AppBook]] =
    Future.successful(books.values.slice(page * size, page * size + size).toSeq)

  def findById(id: UUID): Future[Option[AppBook]] = Future.successful(books.get(id))

  def update(book: AppBook): Future[Option[AppBook]] = {
    val result =
      for {
        id <- book.id
        _  <- books.get(id)
      } yield {
        val authors = book.authors.map(_.copy(id = Some(UUID.randomUUID())))
        val bookUpdated = book.copy(authors = authors)
        books += (id -> bookUpdated)
        bookUpdated
      }

    Future.successful(result)
  }

  def delete(id: UUID): Future[Option[UUID]] =
    books.get(id) match {
      case Some(book) =>
        books -= id
        Future.successful(book.id)
      case None => Future.successful(None)
    }

  def findAllByAuthorLastName(lastName: String): Future[Seq[AppBook]] =
    Future.successful(
      books
        .values
        .filter(_.authors.exists(_.hasLastName(lastName)))
        .toSeq
    )

  def findAuthorsPublishedIn(year: Int): Future[Seq[Author]] =
    Future.successful(
      books
        .values
        .filter(_.isPublishedIn(year))
        .flatMap(_.authors)
        .toSeq
        .distinct
    )

  def findAllWithPagesNumberGreaterThan(pages: Int): Future[Seq[AppBook]] =
    Future.successful(
      books
        .values
        .filter(_.pagesNumber > pages)
        .toSeq
    )

  def findAuthorsWithBooksPagesLessThan(pages: Int, amongAuthors: Set[Author]): Future[Seq[Author]] =
    Future.successful(
      books
        .values
        .filter(book => book.pagesNumber < pages && amongAuthors.toSeq.intersect(book.authors).nonEmpty)
        .flatMap(_.authors)
        .toSeq
        .distinct
    )

  def deleteAllWithAuthors(): Future[Int] = {
    val size = books.size

    books = Map.empty

    Future.successful(size)
  }
}
