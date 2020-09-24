package ru.otus.scala.repository.impl.doobie_quill.author.dao

import java.util.UUID

import cats.syntax.applicative._
import doobie.free.connection.ConnectionIO
import doobie.quill.DoobieContext
import io.getquill.SnakeCase
import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.model.domain.author.Author
import ru.otus.scala.repository.impl
import ru.otus.scala.repository.impl.doobie_quill.author.AuthorDoobieDao
import ru.otus.scala.repository.impl.{AuthorModel, Book, BookAuthor}

class AuthorQuillDao extends AuthorDoobieDao {
  val dc = new DoobieContext.Postgres(SnakeCase)
  import dc._

  private val bookTable = quote { querySchema[Book]("book") }

  private val bookAuthorTable = quote { querySchema[BookAuthor]("book_author") }
  private val authorTable = quote { querySchema[AuthorModel]("author") }
  implicit class ForUpdate[T](q: Query[T]) {
    def forUpdate = quote(infix"$q FOR UPDATE".as[Query[T]])
  }

  def create(author: Author): ConnectionIO[UUID] =
    run {
      quote {
        authorTable
          .insert(lift(AuthorModel(UUID.randomUUID(), author.firstName, author.lastName)))
          .returningGenerated(_.id)
      }
    }

  def addAll(authors: Seq[Author]): ConnectionIO[List[Author]] = {
    val toInsert = authors.toList.map(AuthorModel.fromAuthor)

    run {
      quote {
        liftQuery(toInsert)
          .foreach(a => authorTable.insert(a).returning(a => a))
      }
    }
      .map(_.toList)
      .map(_.map(_.toAuthor))
  }

  def addBookAuthors(book: AppBook, authors: Seq[Author]): ConnectionIO[List[Author]] =
    book
      .id
      .map(bookId =>
        for {
          authors <- addAll(authors)
          _ <-  bindAuthorsToBook(authors, bookId)
        } yield authors
      )
      .getOrElse(List[Author]().pure[ConnectionIO])

  def bindAuthorsToBook(authors: Seq[Author], bookId: UUID): ConnectionIO[Int] =
    run {
      quote {
        liftQuery(
          authors.map(author => impl.BookAuthor(bookId, author.id.get))
        ).foreach(bookAuthor => bookAuthorTable.insert(bookAuthor))
      }
    }.map(_.size)

  def findByFirstAndLastName(firstName: String, lastName: String): ConnectionIO[Option[Author]] =
    run {
      quote {
        authorTable.filter(author => author.firstName == lift(firstName) && author.lastName == lift(lastName))
      }
    }
      .map(_.headOption.map(_.toAuthor))

  def findByBookId(bookId: UUID): ConnectionIO[List[Author]] = {
    run {
      quote {
        authorTable
          .join(bookAuthorTable)
          .on(_.id == _.authorId)
          .filter(_._2.bookId == lift(bookId))
      }
    }.map(_.map(_._1.toAuthor))
  }

  def findAuthorsOf(bookIds: Seq[UUID]): ConnectionIO[Seq[(Author, UUID)]] =
    run {
      quote {
        authorTable.join(bookAuthorTable).on(_.id == _.authorId)
          .filter{ case (author, bookAuthor) => liftQuery(bookIds).contains(bookAuthor.bookId) }
      }
    }.map(_.map { case (authorModel, bookAuthor) => (authorModel.toAuthor, bookAuthor.bookId) })

  def findAllWithPagesNumberLessThan(pagesNumber: Index, amongAuthors: Set[Author]): ConnectionIO[Seq[Author]] =
    run {
      quote {
        authorTable
          .join(bookAuthorTable).on(_.id == _.authorId)
          .join(bookTable).on(_._2.bookId == _.id)
          .filter{
            case ((author, bookAuthor), book) =>
              book.pagesNumber < lift(pagesNumber) && liftQuery(amongAuthors.map(_.id.get)).contains(author.id)
          }
      }
    }.map(_.map(_._1._1.toAuthor))

  def deleteBookAuthors(authorsIds: Seq[UUID], bookId: UUID): ConnectionIO[Int] =
    run {
      quote {
        bookAuthorTable
          .filter(
            bookAuthor => bookAuthor.bookId == lift(bookId) && liftQuery(authorsIds).contains(bookAuthor.authorId)
          )
          .delete
      }
    }.map(_.intValue)

  def deleteAll(): ConnectionIO[Int] =
    run {
      quote {
        authorTable.delete
      }
    }.map(_.intValue)

  def findAllPublishedIn(year: Index): ConnectionIO[Seq[Author]] = ???

  def publishedIn(year: Int): ConnectionIO[Seq[Author]] =
    run {
      quote {
        authorTable
          .join(bookAuthorTable).on(_.id == _.authorId)
          .join(bookTable).on(_._2.bookId == _.id)
          .filter{
            case ((author, bookAuthor), book) => book.yearOfPublishing.contains(lift(year))
          }
      }
    }.map(_.map(_._1._1.toAuthor))
}
