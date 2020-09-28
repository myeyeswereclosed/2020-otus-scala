package ru.otus.scala.repository.impl.doobie_quill.book

import java.util.UUID

import cats.effect._
import cats.instances.list._
import cats.syntax.applicative._
import cats.syntax.apply._
import cats.syntax.option._
import cats.syntax.traverse._
import doobie.free.connection.ConnectionIO
import doobie.syntax.connectionio._
import doobie.util.transactor.Transactor
import ru.otus.scala.model.domain.{AppBook, AppAuthor}
import AppAuthor.Author
import ru.otus.scala.repository.BookRepository
import ru.otus.scala.repository.impl.doobie_quill.author.AuthorDoobieDao

import scala.concurrent.{ExecutionContext, Future}

class BookDoobieRepository(
  bookDao: BookDoobieDao,
  authorDao: AuthorDoobieDao,
  transactor: Transactor[IO]
)(implicit ec: ExecutionContext) extends BookRepository {
  def create(book: AppBook): Future[AppBook] = {
    val queries =
      for {
        storedAuthors <- findAllWithFirstAndLastNames(book.authors)
        bookId <- bookDao.save(book)
        bookCreated = book.copy(id = Some(bookId))
        authors <-
          authorDao.addBookAuthors(
            bookCreated,
            book
              .authors
              .filterNot(author => storedAuthors.exists(_.hasFirstAndLastName(author.firstName, author.lastName)))
          )
        _ <- authorDao.bindAuthorsToBook(storedAuthors, bookId)
      } yield bookCreated.copy(authors = storedAuthors ++ authors)

    queries.transact(transactor).unsafeToFuture()
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

    result.transact(transactor).unsafeToFuture()
  }

  def findById(id: UUID): Future[Option[AppBook]] = {
    val queries =
      for {
        bookModel <- bookDao.findById(id)
        authors <- bookModel.map(model => authorDao.findByBookId(model.id)).getOrElse(List().pure[ConnectionIO])
      } yield bookModel.map(_.toBook(authors))

    queries.transact(transactor).unsafeToFuture()
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
                case None => Option.empty[AppBook].pure[ConnectionIO]
              }
            } yield update

          result.transact(transactor).unsafeToFuture()
        }
      )
      .getOrElse(Future.successful(None))

  private def updateBook(bookStored: AppBook, book: AppBook): ConnectionIO[Option[AppBook]] =
    for {
      bookUpdated <- bookDao.update(book)
      authors <- updateAuthors(bookStored, book.authors)
    } yield bookUpdated.copy(authors = authors).some

  private def updateAuthors(
                             book: AppBook,
                             authorsForUpdate: Seq[Author]
  ): ConnectionIO[List[Author]] = {
    val toDelete = book.authors.filterNot(author => authorsForUpdate.map(_.fullName).contains(author.fullName))
    val newAuthors = authorsForUpdate.diff(toDelete)

    for {
      storedOnes <- findAllWithFirstAndLastNames(newAuthors)
      newOnes = newAuthors.filterNot(author => storedOnes.map(_.fullName).contains(author.fullName))
      _ <- authorDao.deleteBookAuthors(toDelete.map(_.id.get), book.id.get)
      newOnesStored <- authorDao.addBookAuthors(book, newOnes)
      _ <- authorDao.bindAuthorsToBook(storedOnes.filterNot(book.isWrittenBy), book.id.get)
    } yield storedOnes ++ newOnesStored
  }

  def delete(id: UUID): Future[Option[UUID]] =
    bookDao.delete(id).transact(transactor).unsafeToFuture()

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

    result.transact(transactor).unsafeToFuture()
  }

  def findAuthorsPublishedIn(year: Int): Future[Seq[Author]] =
    authorDao.publishedIn(year).transact(transactor).unsafeToFuture()

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

    result.transact(transactor).unsafeToFuture()
  }

  def findAuthorsWithBooksPagesLessThan(pages: Int, amongAuthors: Set[Author]): Future[Seq[Author]] =
    authorDao
      .findAllWithPagesNumberLessThan(pages, amongAuthors)
      .transact(transactor)
      .unsafeToFuture()

  def deleteAllWithAuthors(): Future[Int] =
    (authorDao.deleteAll() *> bookDao.deleteAll()).transact(transactor).unsafeToFuture()

  private def findAllWithFirstAndLastNames(authors: Seq[Author]) =
    authors
      .toList
      .traverse(author => authorDao.findByFirstAndLastName(author.firstName, author.lastName))
      .map(_.filter(_.nonEmpty).map(_.get))

}
