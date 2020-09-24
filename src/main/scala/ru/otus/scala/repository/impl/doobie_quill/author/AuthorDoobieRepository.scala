package ru.otus.scala.repository.impl.doobie_quill.author

import cats.effect.IO
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import ru.otus.scala.model.domain.author.Author
import ru.otus.scala.repository.AuthorRepository
import ru.otus.scala.repository.dao.AuthorDao

import scala.concurrent.Future

class AuthorDoobieRepository(dao: AuthorDao[ConnectionIO], transactor: Transactor[IO]) extends AuthorRepository {
  def create(author: Author): Future[Author] =
    dao
      .create(author)
      .transact(transactor)
      .map(id => author.copy(id = Some(id)))
      .unsafeToFuture()

  def findByFirstAndLastName(firstName: String, lastName: String): Future[Option[Author]] =
    dao
      .findByFirstAndLastName(firstName, lastName)
      .transact(transactor)
      .unsafeToFuture()

  def deleteAll(): Future[Int] =
    dao.deleteAll().transact(transactor).unsafeToFuture()
}
