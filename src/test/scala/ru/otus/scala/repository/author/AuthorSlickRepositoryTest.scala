package ru.otus.scala.repository.author

import cats.effect.{Blocker, ContextShift, IO}
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie.util.ExecutionContexts
import ru.otus.scala.AppConfig.DbConfig
import ru.otus.scala.db.Migration
import ru.otus.scala.repository.AuthorRepository
import ru.otus.scala.repository.impl.doobie_quill.author.AuthorDoobieRepository
import ru.otus.scala.repository.impl.doobie_quill.author.dao.AuthorQuillDao
import ru.otus.scala.repository.impl.slick.author.dao.{AuthorSlickDao, AuthorSlickRepository}
import ru.otus.scala.repository.impl.slick.book.BookSlickRepository
import ru.otus.scala.repository.impl.slick.book.dao.BookSlickDao
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext

class AuthorSlickRepositoryTest
  extends AuthorRepositoryTest(name = "AuthorSlickRepositoryTest")
    with ForAllTestContainer {

  val container: PostgreSQLContainer = PostgreSQLContainer()

  var db: Database = _

  override def afterStart(): Unit = {
    super.afterStart()
    new Migration(DbConfig(container.jdbcUrl, container.username, container.password)).run()

    db = Database.forURL(container.jdbcUrl, container.username, container.password)
  }

  override def beforeStop(): Unit = {
    db.close()
    super.beforeStop()
  }

  def createRepository(): AuthorRepository = {
    implicit val ec: ExecutionContext = ExecutionContexts.synchronous

    val repository = new AuthorSlickRepository(new AuthorSlickDao(), db)
    repository.deleteAll().futureValue
    repository
  }

}
