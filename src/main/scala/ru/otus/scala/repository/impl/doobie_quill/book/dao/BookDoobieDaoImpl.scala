package ru.otus.scala.repository.impl.doobie_quill.book.dao

import java.util.UUID

import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.repository.impl.Book
import ru.otus.scala.repository.impl.doobie_quill.book.BookDoobieDao

class BookDoobieDaoImpl extends BookDoobieDao {
  def save(book: AppBook): ConnectionIO[UUID] = {
    sql"""insert into book(title, pages_number, year_of_publishing)
         values(${book.title}, ${book.pagesNumber}, ${book.yearOfPublishing})"""
      .update
      .withGeneratedKeys[UUID]("id")
      .compile
      .lastOrError
  }

  def findById(id: UUID): ConnectionIO[Option[Book]] =
      selectById(id).query[Book].option

  def findAll(page: Int, size: Int): ConnectionIO[Seq[Book]] = {
    sql"""select b.id, b.title, b.pages_number, b.year_of_publishing from book b
         limit $size offset ${page * size}
       """
      .query[Book]
      .to[Seq]
  }

  def update(book: AppBook): ConnectionIO[AppBook] =
    sql"""update book set
      title = ${book.title},
      pages_number = ${book.pagesNumber},
      year_of_publishing = ${book.yearOfPublishing}
      where id = ${book.id}
    """
    .update
    .run
    .map(_ => book)

  def delete(id: UUID): ConnectionIO[Option[UUID]] = {
    val deleteBookAuthors = sql"""delete from book_author where book_id = $id""".update.run
    val deleteBook = sql"""delete from book where id = $id""".update.run

    for {
      book <- findByIdForUpdate(id)
      result <-
        book
          .map(_ => deleteBookAuthors *> deleteBook *> id.some.pure[ConnectionIO])
          .getOrElse(None.pure[ConnectionIO])
    } yield result
  }

  def findAllByAuthorLastName(lastName: String): ConnectionIO[Seq[Book]] = {
    sql"""
         select distinct b.id, b.title, b.pages_number, b.year_of_publishing
         from author a
         join book_author ba on ba.author_id = a.id
         join book b on ba.book_id = b.id
         where a.last_name = $lastName
    """
      .query[Book]
      .to[Seq]
  }

  def findAllWithPagesNumberGreaterThan(pagesNumber: Int): ConnectionIO[Seq[Book]] = {
    sql"""select b.id, b.title, b.pages_number, b.year_of_publishing
         from book b
         where pages_number > $pagesNumber
     """
      .query[Book]
      .to[Seq]
  }

  def deleteAll(): ConnectionIO[Int] =
    sql"""delete from book""".update.run

  def findByIdForUpdate(id: UUID): ConnectionIO[Option[Book]] =
    (selectById(id) ++ fr" for update").query[Book].option

  private def selectById(id: UUID): Fragment =
    sql"""
         select b.id, b.title, b.pages_number, b.year_of_publishing
         from book b where b.id = $id
    """
}
