package ru.otus.scala.repository.author

import cats.effect.{Blocker, ContextShift, IO}
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie.Transactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor.Aux
import ru.otus.scala.config.AppConfig.DbConfig
import ru.otus.scala.db.Migration
import ru.otus.scala.repository.{AuthorRepository, BookRepository}
import ru.otus.scala.repository.impl.doobie_quill.author.AuthorDoobieRepository
import ru.otus.scala.repository.impl.doobie_quill.author.dao.AuthorQuillDao
import ru.otus.scala.repository.impl.doobie_quill.book.BookDoobieRepository
import ru.otus.scala.repository.impl.doobie_quill.book.dao.BookQuillDao

import scala.concurrent.ExecutionContext

class AuthorQuillRepositoryTest
  extends AuthorRepositoryTest(name = "AuthorQuillRepositoryTest")
    with ForAllTestContainer {
  implicit val ec: ExecutionContext = ExecutionContexts.synchronous
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  val container: PostgreSQLContainer = PostgreSQLContainer()

  override def afterStart(): Unit = {
    super.afterStart()
    new Migration(DbConfig(container.jdbcUrl, container.username, container.password)).run()
  }

  def createRepositories(): (AuthorDoobieRepository, BookDoobieRepository) = {
    val transactor = makeTransactor()

    val dao = new AuthorQuillDao()

    val repository = new AuthorDoobieRepository(dao, transactor)
    val bookRepository = new BookDoobieRepository(new BookQuillDao(), dao, transactor)

    bookRepository.deleteAllWithAuthors()

    (repository, bookRepository)
  }

  private def makeTransactor(): Aux[IO, Unit] =
      Transactor.fromDriverManager[IO](
        "org.postgresql.Driver",
        container.jdbcUrl,
        container.username,
        container.password,
        Blocker.liftExecutionContext(ExecutionContexts.synchronous)
      )
}
