package ru.otus.scala.repository.impl.slick.book

import java.util.UUID

import cats.syntax.option._
import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.model.domain.author.Author
import ru.otus.scala.repository.BookRepository
import ru.otus.scala.repository.impl.slick.DbAction
import ru.otus.scala.repository.impl.slick.DbAction.DbAction
import ru.otus.scala.repository.impl.slick.author.dao.AuthorSlickDao
import ru.otus.scala.repository.impl.slick.book.dao.BookSlickDao
import slick.dbio.DBIOAction
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class BookSlickRepository(bookDao: BookSlickDao,
                          authorDao: AuthorSlickDao,
                          db: Database
                         )(implicit ec: ExecutionContext) extends BookRepository {
  def create(book: AppBook): Future[AppBook] = {
    val queries =
      for {
        existingAuthors <- findAllWithFirstAndLastNames(book.authors)
        t = existingAuthors.filter(_.isDefined).map(_.get)
        bookId <- bookDao.save(book)
        bookCreated = book.copy(id = Some(bookId))
        authors <-
          authorDao.addBookAuthors(
            bookCreated,
            book.authors.filterNot(author => t.map(_.name).contains(author.name))
          )
        _ <- authorDao.bindAuthorsToBook(t, bookId)
      } yield bookCreated.copy(authors = t ++ authors)

    db.run(queries.transactionally)
  }

  def findAll(page: Int, size: Int): Future[Seq[AppBook]] = {
    val result =
      for {
        books <- bookDao.findAll(page, size)
        authors <- authorDao.findAuthorsOf(books.map(_.id))
      } yield
        books.map(
          bookModel =>
            bookModel.toBook(authors.filter { case (author, bookId) => bookId == bookModel.id }.map(_._1))
        )

    db.run(result)
  }

  def findById(id: UUID): Future[Option[AppBook]] = {
    val queries =
      for {
        bookModel <- bookDao.findById(id)
        authors <- bookModel.map(model => authorDao.findByBookId(model.id)).getOrElse(DbAction.success(List()))
      } yield bookModel.map(_.toBook(authors))

    db.run(queries)
  }

  def update(book: AppBook): Future[Option[AppBook]] =
    book
      .id
      .map(
        bookId => {
          val result =
            for {
              bookModel <- bookDao.findById(bookId)
              authors <- authorDao.findByBookId(bookId)
              update <- bookModel.map(_.toBook(authors)) match {
                case Some(value) => updateBook(value, book)
                case None => DbAction.success(None)
              }
            } yield update

          db.run(result.transactionally)
        }
      )
      .getOrElse(Future.successful(None))

  private def updateBook(bookStored: AppBook, book: AppBook): DbAction[Option[AppBook]] =
    for {
      bookUpdated <- bookDao.update(book)
      authors <- updateAuthors(bookStored, book.authors)
    } yield bookUpdated.copy(authors = authors).some

  private def updateAuthors(
                             book: AppBook,
                             authorsForUpdate: Seq[Author]
                           ): DbAction[List[Author]] = {
    val toDelete = book.authors.filterNot(author => authorsForUpdate.map(_.fullName).contains(author.fullName))
    val newAuthors = authorsForUpdate.diff(toDelete)

    for {
      storedOnes <- findAllWithFirstAndLastNames(newAuthors)
      t = storedOnes.filter(_.isDefined).map(_.get)
      newOnes = newAuthors.filterNot(author => t.map(_.fullName).contains(author.fullName))
      _ <- authorDao.deleteBookAuthors(toDelete.map(_.id.get), book.id.get)
      newOnesStored <- authorDao.addBookAuthors(book, newOnes)
      _ <- authorDao.bindAuthorsToBook(t.filterNot(book.isWrittenBy), book.id.get)
    } yield t ++ newOnesStored
  }

  def delete(id: UUID): Future[Option[UUID]] =
    db.run(bookDao.delete(id))

  def findAllByAuthorLastName(lastName: String): Future[Seq[AppBook]] = {
    val result =
      for {
        bookModels <- bookDao.findAllByAuthorLastName(lastName)
        authors <- authorDao.findAuthorsOf(bookModels.map(_.id))
      } yield
        bookModels.map(
          model =>
            model.toBook(authors.filter { case (author, bookId) => bookId == model.id }.map(_._1))
        )

    db.run(result)
  }

  def findAuthorsPublishedIn(year: Int): Future[Seq[Author]] =
    db.run(authorDao.findAllPublishedIn(year))

  def findAllWithPagesNumberGreaterThan(pages: Int): Future[Seq[AppBook]] = {
    val result =
      for {
        books <- bookDao.findAllWithPagesNumberGreaterThan(pages)
        authors <- authorDao.findAuthorsOf(books.map(_.id))
      } yield
        books.map(
          bookModel =>
            bookModel.toBook(authors.filter { case (author, bookId) => bookId == bookModel.id }.map(_._1))
        )

    db.run(result)
  }

  def findAuthorsWithBooksPagesLessThan(pages: Int, amongAuthors: Set[Author]): Future[Seq[Author]] =
    db.run(authorDao.findAllWithPagesNumberLessThan(pages, amongAuthors))

  def deleteAllWithAuthors(): Future[Int] = {
    val queries =
      for {
        _ <- authorDao.deleteAll()
        deleted <- bookDao.deleteAll()
      } yield deleted

    db.run(queries.transactionally)
  }

  private def findAllWithFirstAndLastNames(authors: Seq[Author]): DbAction[List[Option[Author]]] =
    DBIOAction.sequence(
      authors
        .toList
        .map(author => authorDao.findByFirstAndLastName(author.firstName, author.lastName))
    )
}

