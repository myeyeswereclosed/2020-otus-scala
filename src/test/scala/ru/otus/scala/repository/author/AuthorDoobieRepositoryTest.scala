package ru.otus.scala.repository.author

import cats.effect.{Blocker, ContextShift, IO}
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie.Transactor
import doobie.util.ExecutionContexts
import ru.otus.scala.config.AppConfig.DbConfig
import ru.otus.scala.db.Migration
import ru.otus.scala.repository.AuthorRepository
import ru.otus.scala.repository.impl.doobie_quill.author.AuthorDoobieRepository
import ru.otus.scala.repository.impl.doobie_quill.author.dao.AuthorDoobieDaoImpl
import ru.otus.scala.repository.impl.doobie_quill.book.dao.BookDoobieDaoImpl
import ru.otus.scala.repository.impl.doobie_quill.book.{BookDoobieDao, BookDoobieRepository}

import scala.concurrent.ExecutionContext

class AuthorDoobieRepositoryTest
  extends AuthorRepositoryTest(name = "AuthorDoobieRepositoryTest")
    with ForAllTestContainer {
  val container: PostgreSQLContainer = PostgreSQLContainer()

  override def afterStart(): Unit = {
    super.afterStart()
    new Migration(DbConfig(container.jdbcUrl, container.username, container.password)).run()
  }

  def createRepositories(): (AuthorDoobieRepository, BookDoobieRepository) = {
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

    val authorDao = new AuthorDoobieDaoImpl()

    val repository = new AuthorDoobieRepository(authorDao, transactor)
    val bookRepository = new BookDoobieRepository(new BookDoobieDaoImpl(), authorDao, transactor)

    bookRepository.deleteAllWithAuthors()

    (repository, bookRepository)
  }
}
