package ru.otus.scala.repository.impl.slick.author.dao

import ru.otus.scala.model.domain.AppAuthor
import AppAuthor.Author
import ru.otus.scala.repository.AuthorRepository
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

class AuthorSlickRepository(
  dao: AuthorSlickDao,
  db: Database
)(implicit ec: ExecutionContext) extends AuthorRepository {
  def create(author: Author): Future[Author] =
    db.run(
      dao
        .create(author)
        .map(id => author.copy(id = Some(id)))
    )

  def findByFirstAndLastName(firstName: String, lastName: String): Future[Option[Author]] =
    db.run(dao.findByFirstAndLastName(firstName, lastName))

  def findPublishedIn(year: Int): Future[Seq[Author]] =
    db.run(dao.findAllPublishedIn(year))

  def findAuthorsWithBooksPagesLessThan(pages: Int, amongAuthors: Set[Author]): Future[Seq[Author]] =
    db.run(dao.findAllWithPagesNumberLessThan(pages, amongAuthors))

  def deleteAll(): Future[Int] = db.run(dao.deleteAll())
}
