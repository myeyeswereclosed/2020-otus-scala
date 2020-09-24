package ru.otus.scala.repository.impl.doobie_quill.author

import java.util.UUID

import doobie.free.connection.ConnectionIO
import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.model.domain.author.Author
import ru.otus.scala.repository.dao.AuthorDao

trait AuthorDoobieDao extends AuthorDao[ConnectionIO] {
  def create(author: Author): ConnectionIO[UUID]

  def addAll(authors: Seq[Author]): ConnectionIO[List[Author]]

  def addBookAuthors(book: AppBook, authors: Seq[Author]): ConnectionIO[List[Author]]

  def bindAuthorsToBook(authors: Seq[Author], bookId: UUID): ConnectionIO[Int]

  def findByFirstAndLastName(firstName: String, lastName: String): ConnectionIO[Option[Author]]

  def findByBookId(bookId: UUID): ConnectionIO[List[Author]]

  def findAuthorsOf(bookIds: Seq[UUID]): ConnectionIO[Seq[(Author, UUID)]]

  def publishedIn(year: Int): ConnectionIO[Seq[Author]]

  def findAllWithPagesNumberLessThan(pagesNumber: Int, amongAuthors: Set[Author]): ConnectionIO[Seq[Author]]

  def deleteBookAuthors(authorsIds: Seq[UUID], bookId: UUID): ConnectionIO[Int]

  def deleteAll(): ConnectionIO[Int]
}
