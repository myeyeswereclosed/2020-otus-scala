package ru.otus.scala.repository.dao

import java.util.UUID

import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.model.domain.AppAuthor.Author

trait AuthorDao[F[_]] {
  def create(author: Author): F[UUID]

  def addAll(authors: Seq[Author]): F[List[Author]]

  def addBookAuthors(book: AppBook, authors: Seq[Author]): F[List[Author]]

  def bindAuthorsToBook(authors: Seq[Author], bookId: UUID): F[Int]

  def findByFirstAndLastName(firstName: String, lastName: String): F[Option[Author]]

  def findByBookId(bookId: UUID): F[List[Author]]

  def findAuthorsOf(bookIds: Seq[UUID]): F[Seq[(Author, UUID)]]

  def findAllPublishedIn(year: Int): F[Seq[Author]]

  def findAllWithPagesNumberLessThan(pagesNumber: Int, amongAuthors: Set[Author]): F[Seq[Author]]

  def deleteBookAuthors(authorsIds: Seq[UUID], bookId: UUID): F[Int]

  def deleteAll(): F[Int]
}
