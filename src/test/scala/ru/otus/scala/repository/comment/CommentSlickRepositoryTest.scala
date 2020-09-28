package ru.otus.scala.repository.comment

import cats.effect.{Blocker, ContextShift, IO}
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie.Transactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor.Aux
import ru.otus.scala.config.AppConfig.DbConfig
import ru.otus.scala.db.Migration
import ru.otus.scala.repository.impl.doobie_quill.author.dao.AuthorQuillDao
import ru.otus.scala.repository.impl.doobie_quill.book.BookDoobieRepository
import ru.otus.scala.repository.impl.doobie_quill.book.dao.BookQuillDao
import ru.otus.scala.repository.impl.doobie_quill.comment.CommentDoobieRepository
import ru.otus.scala.repository.impl.doobie_quill.comment.dao.CommentQuillDao
import ru.otus.scala.repository.impl.slick.author.dao.AuthorSlickDao
import ru.otus.scala.repository.impl.slick.book.BookSlickRepository
import ru.otus.scala.repository.impl.slick.book.dao.BookSlickDao
import ru.otus.scala.repository.impl.slick.comment.CommentSlickRepository
import ru.otus.scala.repository.impl.slick.comment.dao.CommentSlickDao
import ru.otus.scala.repository.{BookRepository, CommentRepository}
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext

class CommentSlickRepositoryTest
  extends CommentRepositoryTest("CommentSlickRepositoryTest")
    with ForAllTestContainer {

  val container: PostgreSQLContainer = PostgreSQLContainer()

  implicit val ec: ExecutionContext = ExecutionContexts.synchronous
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

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

  def createRepository(): CommentRepository = {
    val repository = new CommentSlickRepository(new CommentSlickDao, db)

    repository.deleteAll().futureValue

    repository
  }

  def createBookRepository(): Option[BookRepository] = {
    val repository = new BookSlickRepository(new BookSlickDao, new AuthorSlickDao, db)

    repository.deleteAllWithAuthors()

    Some(repository)
  }

}
