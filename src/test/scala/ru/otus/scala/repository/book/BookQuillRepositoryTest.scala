package ru.otus.scala.repository.book

import cats.effect.{Blocker, ContextShift, IO}
import com.dimafeng.testcontainers.{Container, ForAllTestContainer, PostgreSQLContainer}
import doobie.Transactor
import doobie.util.ExecutionContexts
import ru.otus.scala.AppConfig.DbConfig
import ru.otus.scala.db.Migration
import ru.otus.scala.repository.BookRepository
import ru.otus.scala.repository.impl.doobie_quill.author.dao.AuthorQuillDao
import ru.otus.scala.repository.impl.doobie_quill.book.dao.BookQuillDao
import ru.otus.scala.repository.impl.doobie_quill.book.BookDoobieRepository

import scala.concurrent.ExecutionContext

class BookQuillRepositoryTest extends BookRepositoryTest("BookQuillRepositoryTest") with ForAllTestContainer {
  val container: PostgreSQLContainer = PostgreSQLContainer()

  override def afterStart(): Unit = {
    super.afterStart()
    new Migration(DbConfig(container.jdbcUrl, container.username, container.password)).run()
  }

  override def createRepository(): BookRepository = {
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

    val repository = new BookDoobieRepository(new BookQuillDao(), new AuthorQuillDao(), transactor)

    repository.deleteAllWithAuthors().futureValue

    repository
  }

}
