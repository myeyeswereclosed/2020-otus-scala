package ru.otus.scala.repository.book

import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie.util.ExecutionContexts
import ru.otus.scala.AppConfig.DbConfig
import ru.otus.scala.db.Migration
import ru.otus.scala.repository.BookRepository
import ru.otus.scala.repository.impl.slick.author.dao.AuthorSlickDao
import ru.otus.scala.repository.impl.slick.book.BookSlickRepository
import ru.otus.scala.repository.impl.slick.book.dao.BookSlickDao
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext

class BookSlickRepositoryTest extends BookRepositoryTest("BookSlickRepositoryTest") with ForAllTestContainer {

  override val container: PostgreSQLContainer = PostgreSQLContainer()

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

  def createRepository(): BookRepository = {
    implicit val ec: ExecutionContext = ExecutionContexts.synchronous

    val repository = new BookSlickRepository(new BookSlickDao(), new AuthorSlickDao(), db)
    repository.deleteAllWithAuthors().futureValue
    repository
  }
}
