package ru.otus.scala.repository.book

import cats.effect._
import cats.effect._
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie._
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor.Aux
import ru.otus.scala.config.AppConfig.{Config, DbConfig}
import ru.otus.scala.db.Migration
import cats.syntax.traverse._
import cats.syntax.applicative._
import cats.data.NonEmptyList
import cats.syntax.applicative._
import cats.instances.list._
import cats.data.NonEmptyList
import cats.syntax.applicative._
import doobie._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import cats.syntax.traverse._
import cats.instances.list._
import doobie.postgres.implicits._
import ru.otus.scala.repository.BookRepository
import ru.otus.scala.repository.impl.doobie_quill.author.dao.AuthorDoobieDaoImpl
import ru.otus.scala.repository.impl.doobie_quill.book.dao.BookDoobieDaoImpl
import ru.otus.scala.repository.impl.doobie_quill.book.BookDoobieRepository

import scala.concurrent.ExecutionContext

class BookDoobieRepositoryTest extends BookRepositoryTest("BookDoobieRepositoryTest") with ForAllTestContainer {
  val container: PostgreSQLContainer = PostgreSQLContainer()

  override def afterStart(): Unit = {
    super.afterStart()
    new Migration(DbConfig(container.jdbcUrl, container.username, container.password)).run()
  }

  def createRepository(): BookRepository = {
    implicit val ec: ExecutionContext = ExecutionContexts.synchronous
    implicit val cs: ContextShift[IO] = IO.contextShift(ec)

    val transactor =
      Transactor.fromDriverManager[IO](
        "org.postgresql.Driver",                                    // driver classname
        container.jdbcUrl,                                          // connect URL (driver-specific)
        container.username,                                         // user
        container.password,                                         // password
        Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
      )

    val repository = new BookDoobieRepository(new BookDoobieDaoImpl(), new AuthorDoobieDaoImpl(), transactor)

    repository.deleteAllWithAuthors().futureValue

    repository
  }
}
