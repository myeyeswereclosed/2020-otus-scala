package ru.otus.scala.repository.author

import cats.effect.{Blocker, ContextShift, IO}
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie.Transactor
import doobie.util.ExecutionContexts
import ru.otus.scala.AppConfig.DbConfig
import ru.otus.scala.db.Migration
import ru.otus.scala.repository.AuthorRepository
import ru.otus.scala.repository.impl.doobie_quill.author.AuthorDoobieRepository
import ru.otus.scala.repository.impl.doobie_quill.author.dao.AuthorQuillDao

import scala.concurrent.ExecutionContext

class AuthorQuillRepositoryTest
  extends AuthorRepositoryTest(name = "AuthorQuillRepositoryTest")
    with ForAllTestContainer {
  val container: PostgreSQLContainer = PostgreSQLContainer()

  override def afterStart(): Unit = {
    super.afterStart()
    new Migration(DbConfig(container.jdbcUrl, container.username, container.password)).run()
  }

  def createRepository(): AuthorRepository = {
    implicit val ec: ExecutionContext = ExecutionContexts.synchronous
    implicit val cs: ContextShift[IO] = IO.contextShift(ec)

    val transactor =
      Transactor.fromDriverManager[IO](
        "org.postgresql.Driver",
        container.jdbcUrl,
        container.username,
        container.password,
        Blocker.liftExecutionContext(ExecutionContexts.synchronous)
      )

    val repository = new AuthorDoobieRepository(new AuthorQuillDao, transactor)

    repository.deleteAll()

    repository
  }
}
