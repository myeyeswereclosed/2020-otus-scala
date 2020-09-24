import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import ru.otus.scala.greet.dao.impl.GreetingDaoImpl
import ru.otus.scala.greet.router.GreetRouter
import ru.otus.scala.greet.service.impl.GreetingServiceImpl
import ru.otus.scala.route.{Router, RouterImpl}
import cats.effect.{Resource, _}
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.hikari._
import doobie.util.transactor.Transactor
import ru.otus.scala.AppConfig
import ru.otus.scala.AppConfig.{Config, ServerConfig}
import ru.otus.scala.db.Migration
import ru.otus.scala.repository.impl.doobie_quill.author.dao.AuthorDoobieDaoImpl
import ru.otus.scala.repository.impl.doobie_quill.book.dao.BookDoobieDaoImpl
import ru.otus.scala.repository.impl.doobie_quill.comment.dao.CommentDoobieDaoImpl
import ru.otus.scala.repository.impl.doobie_quill.author.AuthorDoobieRepository
import ru.otus.scala.repository.impl.doobie_quill.book.BookDoobieRepository
import ru.otus.scala.repository.impl.doobie_quill.comment.CommentDoobieRepository
import ru.otus.scala.repository.impl.map.{AuthorMapRepository, CommentMapRepository}
import ru.otus.scala.router.{AuthorRouter, BookRouter, CommentRouter}
import ru.otus.scala.service.author.AuthorServiceImpl
import ru.otus.scala.service.book.BookServiceImpl
import ru.otus.scala.service.comment.CommentServiceImpl

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object DoobieMain {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  def main(args: Array[String]): Unit = {
    //    val bookDao = new BookDaoMapImpl
//    val commentDao = new CommentDaoMapImpl
//
//    val bookRouter = new BookRouter(new BookServiceImpl(bookDao, new AuthorDaoMapImpl))
//    val commentRouter = new CommentRouter(new CommentServiceImpl(bookDao, commentDao))
//    val authorRouter = new AuthorRouter(new AuthorServiceImpl(bookDao, commentDao))
//    val greetRouter = new GreetRouter(new GreetingServiceImpl(new GreetingDaoImpl))

    val config = Config.default

    val binding =
      for {
        ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
        be <- Blocker[IO] // our blocking EC
        xa <- HikariTransactor.newHikariTransactor[IO](
          "org.postgresql.Driver", // driver classname
          config.db.url,            // connect URL
          config.db.user,           // username
          config.db.password,       // password
          ce,                      // await connection here
          be                       // execute JDBC operations here
        )
        system <- Resource.make(IO(ActorSystem("system")))(s =>
          IO.fromFuture(IO(s.terminate())).map(_ => ())
        )
        binding <- makeBinding(config.server, xa)(system)
      } yield binding

    val app =
      binding
        .use { binding =>
          for {
            _ <- IO(println(s"Binding on ${binding.localAddress}"))
            _ <- IO(StdIn.readLine())
          } yield ()
        }

    val init = IO(new Migration(config.db).run())

    (init *> app).unsafeRunSync()
  }

  def makeBinding(
                   config:ServerConfig,
                   tr: Transactor[IO]
                 )(implicit system: ActorSystem): Resource[IO, Http.ServerBinding] = {
    Resource
      .make(
        IO.fromFuture(
          IO(
            Http()(system)
              .newServerAt(config.host, config.port)
              .bind(createRouter(tr)(system.dispatcher).route)
          )
        )
      )(b => IO.fromFuture(IO(b.unbind())).map(_ => ()))
  }

  def createRouter(transactor: Transactor[IO])(implicit ec: ExecutionContextExecutor): Router = {
    val bookRepository = new BookDoobieRepository(new BookDoobieDaoImpl(), new AuthorDoobieDaoImpl(), transactor)
    val commentRepository = new CommentDoobieRepository(new CommentDoobieDaoImpl, transactor)
    val authorRepository = new AuthorDoobieRepository(new AuthorDoobieDaoImpl(), transactor)

    val bookRouter = new BookRouter(new BookServiceImpl(bookRepository, authorRepository))
    val commentRouter = new CommentRouter(new CommentServiceImpl(bookRepository, commentRepository))
    val authorRouter = new AuthorRouter(new AuthorServiceImpl(bookRepository, commentRepository))
    val greetRouter = new GreetRouter(new GreetingServiceImpl(new GreetingDaoImpl))

    new RouterImpl(Seq(bookRouter, authorRouter, commentRouter, greetRouter))
  }

}
