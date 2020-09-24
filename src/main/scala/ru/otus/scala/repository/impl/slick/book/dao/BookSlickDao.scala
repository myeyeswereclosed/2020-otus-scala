package ru.otus.scala.repository.impl.slick.book.dao

import java.util.UUID

import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.repository.dao.BookDao
import ru.otus.scala.repository.impl.slick.DbAction
import ru.otus.scala.repository.impl.slick.DbAction.DbAction
import ru.otus.scala.repository.impl.{AuthorModel, Book, BookAuthor}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

class BookSlickDao(implicit ec: ExecutionContext) extends BookDao[DbAction] {
  import ru.otus.scala.repository.impl.slick.Tables._

  def save(book: AppBook): DbAction[UUID] =
    (books returning books.map(_.id)) +=
      Book(
        UUID.randomUUID(),
        title = book.title,
        pagesNumber = book.pagesNumber,
        yearOfPublishing = book.yearOfPublishing
      )

  def findById(id: UUID): DbAction[Option[Book]] =
    books.filter(_.id === id).result.headOption

  def findByIdForUpdate(id: UUID): DbAction[Option[Book]] =
    books.filter(_.id === id).forUpdate.result.headOption

  def findAll(page: Int, size: Int): DbAction[Seq[Book]] =
    books.drop(page * size).take(size).result

  def update(book: AppBook): DbAction[AppBook] =
    books
      .filter(_.id === book.id.get)
      .map(book => (book.title, book.pagesNumber, book.yearOfPublishing))
      .update((book.title, book.pagesNumber, book.yearOfPublishing))
      .map(_ => book)

  def delete(id: UUID): DbAction[Option[UUID]] =
    for {
      book <- findByIdForUpdate(id)
      res <- book match {
        case None => DbAction.success(None)
        case Some(_) =>
          for {
            _ <- bookAuthors.filter(_.bookId === id).delete
            _ <- books.filter(_.id === id).delete
          } yield Some(id)
      }
    } yield res

  def findAllByAuthorLastName(lastName: String): DbAction[Seq[Book]] =
    authors
      .join(bookAuthors).on(_.id === _.authorId)
      .join(books).on(_._2.bookId === _.id)
      .filter(_._1._1.lastName === lastName)
      .map(_._2)
      .result

  def findAllWithPagesNumberGreaterThan(pagesNumber: Int): DbAction[Seq[Book]] =
    books.filter(_.pagesNumber > pagesNumber).result

  def deleteAll(): DbAction[Int] =
    for {
      _ <- bookAuthors.delete
      booksNumber <- books.delete
    } yield booksNumber
}
