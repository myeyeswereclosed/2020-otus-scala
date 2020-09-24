package ru.otus.scala.repository.impl.doobie_quill.book.dao

import java.util.UUID

import cats.syntax.applicative._
import cats.syntax.apply._
import doobie.ConnectionIO
import doobie.quill.DoobieContext
import io.getquill.SnakeCase
import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.repository.impl.doobie_quill.book.BookDoobieDao
import ru.otus.scala.repository.impl.{AuthorModel, Book, BookAuthor}

class BookQuillDao extends BookDoobieDao {
  val dc = new DoobieContext.Postgres(SnakeCase)
  import dc._

  private val bookTable = quote { querySchema[Book]("book") }
  private val bookAuthorTable = quote { querySchema[BookAuthor]("book_author") }
  private val authorTable = quote { querySchema[AuthorModel]("author") }

  implicit class ForUpdate[T](q: Query[T]) {
    def forUpdate = quote(infix"$q FOR UPDATE".as[Query[T]])
  }

  def save(book: AppBook): ConnectionIO[UUID] =
    run {
      quote {
        bookTable.insert(lift(Book.fromBook(book))).returningGenerated(_.id)
      }
    }

  def findById(id: UUID): ConnectionIO[Option[Book]] =
    run {
      quote {
        bookTable.filter(_.id == lift(id))
      }
    }.map(_.headOption)

  def findByIdForUpdate(id: UUID): ConnectionIO[Option[Book]] = {
    run {
      quote {
        query[Book].filter(_.id == lift(id)).forUpdate
      }
    }.map(_.headOption)
  }

  def findAll(page: Int, size: Int): ConnectionIO[Seq[Book]] = {
    run {
      quote {
        bookTable.drop(lift(page * size)).take(lift(size))
      }
    }.map(_.toSeq)
  }

  def update(book: AppBook): ConnectionIO[AppBook] =
    run {
      quote {
        bookTable
          .filter(_.id == lift(book.id.get))
          .update(
            _.title -> lift(book.title),
            _.pagesNumber -> lift(book.pagesNumber),
            _.yearOfPublishing -> lift(book.yearOfPublishing)
          )
      }
    }
      .map(_.intValue)
      .map(_ => book)

    def delete(id: UUID): ConnectionIO[Option[UUID]] = {
      for {
        book <- findByIdForUpdate(id)
        bookAuthors <- findBookAuthors(id)
        _ <- book match {
          case Some(value) =>
            val deleteBookAuthors  =
              run {
                quote {
                  bookAuthorTable.filter(_.bookId == lift(id)).delete
                }
              }

            val deleteBook =
              run {
                quote {
                  bookTable.filter(_.id == lift(id)).delete
                }
              }

            deleteBookAuthors *> deleteBook
          case None => ().pure[ConnectionIO]
        }
      } yield book.map(_.id)
    }

  def findAllByAuthorLastName(lastName: String): ConnectionIO[Seq[Book]] =
    run {
      quote {
        authorTable
          .join(bookAuthorTable).on(_.id == _.authorId)
          .join(bookTable).on(_._2.bookId == _.id)
          .filter(_._1._1.lastName == lift(lastName))
      }
    }.map(_.map(_._2).toSeq)

  def findAllWithPagesNumberGreaterThan(pagesNumber: Int): ConnectionIO[Seq[Book]] =
    run {
      quote {
        bookTable.filter(_.pagesNumber > lift(pagesNumber))
      }
    }.map(_.toSeq)

  def deleteAll(): ConnectionIO[Int] =
    run {
      quote {
        bookTable.delete
      }
    }.map(_.intValue)

  private def findBookAuthors(bookId: UUID): ConnectionIO[Set[BookAuthor]] =
    run {
      quote {
        bookAuthorTable.filter(_.bookId == lift(bookId))
      }
    }.map(_.toSet)

}
