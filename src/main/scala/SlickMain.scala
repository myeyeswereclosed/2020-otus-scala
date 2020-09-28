import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import ru.otus.scala.config.AppConfig.Config
import ru.otus.scala.repository.impl.slick.author.dao.{AuthorSlickDao, AuthorSlickRepository}
import ru.otus.scala.repository.impl.slick.book.BookSlickRepository
import ru.otus.scala.repository.impl.slick.book.dao.BookSlickDao
import ru.otus.scala.repository.impl.slick.comment.CommentSlickRepository
import ru.otus.scala.repository.impl.slick.comment.dao.CommentSlickDao
import ru.otus.scala.router.{AuthorRouter, BookRouter, CommentRouter, Router, AppRouter}
import ru.otus.scala.service.author.AuthorServiceImpl
import ru.otus.scala.service.book.BookServiceImpl
import ru.otus.scala.service.comment.CommentServiceImpl
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.util.Using

object SlickMain {
  def main(args: Array[String]): Unit = {

    val config = Config.default

    implicit val system: ActorSystem = ActorSystem("system")
    import system.dispatcher

    Using.resource(Database.forURL(config.db.url, config.db.user, config.db.password)) { db =>
      val binding =
        Http()
          .newServerAt(config.server.host, config.server.port)
          .bind(createRouter(db).route)

      binding.foreach(b => println(s"Binding on ${b.localAddress}"))

      StdIn.readLine()

      binding.map(_.unbind()).onComplete(_ -> system.terminate())
    }
  }

  def createRouter(db: Database)(implicit ec: ExecutionContextExecutor): Router = {
    val bookRepository = new BookSlickRepository(new BookSlickDao(), new AuthorSlickDao(), db)
    val commentRepository = new CommentSlickRepository(new CommentSlickDao(), db)
    val authorRepository = new AuthorSlickRepository(new AuthorSlickDao(), db)

    val bookRouter = new BookRouter(new BookServiceImpl(bookRepository, authorRepository))
    val commentRouter = new CommentRouter(new CommentServiceImpl(bookRepository, commentRepository))
    val authorRouter = new AuthorRouter(new AuthorServiceImpl(authorRepository))

    new AppRouter(Seq(bookRouter, authorRouter, commentRouter))
  }
}
