package ru.otus.scala.repository.comment

import cats.effect.{Blocker, ContextShift, IO}
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie.Transactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor.Aux
import ru.otus.scala.config.AppConfig.DbConfig
import ru.otus.scala.db.Migration
import ru.otus.scala.repository.{BookRepository, CommentRepository}
import ru.otus.scala.repository.impl.doobie_quill.author.dao.AuthorQuillDao
import ru.otus.scala.repository.impl.doobie_quill.book.dao.BookQuillDao
import ru.otus.scala.repository.impl.doobie_quill.comment.dao.CommentQuillDao
import ru.otus.scala.repository.impl.doobie_quill.book.BookDoobieRepository
import ru.otus.scala.repository.impl.doobie_quill.comment.CommentDoobieRepository

import scala.concurrent.ExecutionContext

class CommentQuillRepositoryTest extends CommentRepositoryTest("CommentQuillRepositoryTest") with ForAllTestContainer {
  val container: PostgreSQLContainer = PostgreSQLContainer()

  implicit val ec: ExecutionContext = ExecutionContexts.synchronous
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  override def afterStart(): Unit = {
    super.afterStart()
    new Migration(DbConfig(container.jdbcUrl, container.username, container.password)).run()
  }

  def createRepository(): CommentRepository = {
    val repository = new CommentDoobieRepository(new CommentQuillDao, makeTransactor())

    repository.deleteAll().futureValue

    repository
  }

  def createBookRepository(): Option[BookRepository] = {
    val repository = new BookDoobieRepository(new BookQuillDao, new AuthorQuillDao, makeTransactor())

    repository.deleteAllWithAuthors()

    Some(repository)
  }

  private def makeTransactor(): Aux[IO, Unit] = {
    Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      container.jdbcUrl,
      container.username,
      container.password,
      Blocker.liftExecutionContext(ExecutionContexts.synchronous)
    )
  }
}
