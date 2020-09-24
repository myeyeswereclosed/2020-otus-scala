package ru.otus.scala.repository.impl.slick.author.dao

import java.util.UUID

import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.model.domain.author.Author
import ru.otus.scala.repository.dao.AuthorDao
import ru.otus.scala.repository.impl.slick.DbAction
import DbAction.DbAction
import ru.otus.scala.repository.impl.{AuthorModel, BookAuthor}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

class AuthorSlickDao(implicit ec: ExecutionContext) extends AuthorDao[DbAction] {
  import ru.otus.scala.repository.impl.slick.Tables._

  def create(author: Author): DbAction[UUID] =
    (authors returning authors.map(_.id)) +=
      AuthorModel(
        UUID.randomUUID(),
        firstName = author.firstName,
        lastName = author.lastName
      )

  def addAll(authorsToAdd: Seq[Author]): DbAction[List[Author]] =
    ((authors returning authors) ++=
      authorsToAdd.map(author => AuthorModel(UUID.randomUUID(), author.firstName, author.lastName)))
        .map(_.map(_.toAuthor).toList)

  def addBookAuthors(book: AppBook, authors: Seq[Author]): DbAction[List[Author]] =
    book
      .id
      .map(bookId =>
        for {
          authors <- addAll(authors)
          _ <-  bindAuthorsToBook(authors, bookId)
        } yield authors
      )
      .getOrElse(DbAction.success(List()))

  def bindAuthorsToBook(authors: Seq[Author], bookId: UUID): DbAction[Int] =
    (bookAuthors ++= authors.map(author => BookAuthor(bookId, author.id.get))).map(_.size)

  def findByFirstAndLastName(firstName: String, lastName: String): DbAction[Option[Author]] = {
    authors
      .filter(author => author.firstName === firstName && author.lastName === lastName)
      .result
      .headOption
      .map(_.map(_.toAuthor))
  }

  def findByBookId(bookId: UUID): DbAction[List[Author]] =
    authors
      .join(bookAuthors).on(_.id === _.authorId)
      .join(books).on(_._2.bookId === _.id)
      .filter(_._2.id === bookId)
      .map(_._1._1)
      .result
      .map(_.map(_.toAuthor).toList)

  def findAuthorsOf(bookIds: Seq[UUID]): DbAction[Seq[(Author, UUID)]] =
    authors
      .join(bookAuthors).on(_.id === _.authorId)
      .filter(_._2.bookId inSet bookIds)
      .result
      .map(_.map{
        case (authorModel, bookAuthor) => (authorModel.toAuthor, bookAuthor.bookId)
      })


  def findAllPublishedIn(year: Int): DbAction[Seq[Author]] =
    authors
      .join(bookAuthors).on(_.id === _.authorId)
      .join(books).on(_._2.bookId === _.id)
      .filter(_._2.yearOfPublishing.map(_ === year).getOrElse(false))
      .result
      .map(_.toSeq)
      .map(_.map(_._1._1).map(_.toAuthor))

  def findAllWithPagesNumberLessThan(pagesNumber: Int, amongAuthors: Set[Author]): DbAction[Seq[Author]] =
    authors
      .join(bookAuthors).on(_.id === _.authorId)
      .join(books).on(_._2.bookId === _.id)
      .filter {
        case ((author, bookAuthor), book) =>
          book.pagesNumber < pagesNumber && (author.id inSet amongAuthors.map(_.id).filter(_.nonEmpty).map(_.get))
      }
      .result
      .map(_.toSeq)
      .map(_.map(_._1._1).map(_.toAuthor))

  def deleteBookAuthors(authorsIds: Seq[UUID], bookId: UUID): DbAction[Int] =
    bookAuthors
      .filter(bookAuthor => bookAuthor.bookId === bookId && (bookAuthor.authorId inSet authorsIds))
      .delete

  def deleteAll(): DbAction[Int] =
    authors.delete
}
