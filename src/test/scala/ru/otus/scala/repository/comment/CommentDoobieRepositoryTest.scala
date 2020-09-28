package ru.otus.scala.repository.comment

import cats.effect.{Blocker, ContextShift, IO}
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie.Transactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor.Aux
import ru.otus.scala.config.AppConfig.DbConfig
import ru.otus.scala.db.Migration
import ru.otus.scala.repository.{BookRepository, CommentRepository}
import ru.otus.scala.repository.impl.doobie_quill.author.dao.AuthorDoobieDaoImpl
import ru.otus.scala.repository.impl.doobie_quill.book.dao.BookDoobieDaoImpl
import ru.otus.scala.repository.impl.doobie_quill.comment.dao.CommentDoobieDaoImpl
import ru.otus.scala.repository.impl.doobie_quill.book.BookDoobieRepository
import ru.otus.scala.repository.impl.doobie_quill.comment.CommentDoobieRepository

import scala.concurrent.ExecutionContext

class CommentDoobieRepositoryTest extends CommentRepositoryTest("CommentDoobieRepositoryTest") with ForAllTestContainer {
  val container: PostgreSQLContainer = PostgreSQLContainer()

  implicit val ec: ExecutionContext = ExecutionContexts.synchronous
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  override def afterStart(): Unit = {
    super.afterStart()
    new Migration(DbConfig(container.jdbcUrl, container.username, container.password)).run()
  }

  def createRepository(): CommentRepository = {
    val repository = new CommentDoobieRepository(new CommentDoobieDaoImpl(), makeTransactor())

    repository.deleteAll().futureValue

    repository
  }

  def createBookRepository(): Option[BookRepository] = {
    val repository = new BookDoobieRepository(new BookDoobieDaoImpl, new AuthorDoobieDaoImpl, makeTransactor())

    repository.deleteAllWithAuthors()

    Some(repository)
  }

  private def makeTransactor(): Aux[IO, Unit] = {
    Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",                                    // driver classname
      container.jdbcUrl,                                          // connect URL (driver-specific)
      container.username,                                         // user
      container.password,                                         // password
      Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
    )
  }
}
