import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import ru.otus.scala.book.dao.author.AuthorDaoMapImpl
import ru.otus.scala.book.dao.book.BookDaoMapImpl
import ru.otus.scala.book.dao.comment.CommentDaoMapImpl
import ru.otus.scala.book.router.{AuthorRouter, BookRouter, CommentRouter}
import ru.otus.scala.book.service.author.AuthorServiceImpl
import ru.otus.scala.book.service.book.BookServiceImpl
import ru.otus.scala.book.service.comment.CommentServiceImpl
import ru.otus.scala.greet.dao.impl.GreetingDaoImpl
import ru.otus.scala.greet.router.GreetRouter
import ru.otus.scala.greet.service.impl.GreetingServiceImpl
import ru.otus.scala.route.RouterImpl

import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("system")

    import system.dispatcher

    val host = "localhost"
    val port = 8080

    val bookDao = new BookDaoMapImpl
    val commentDao = new CommentDaoMapImpl

    val bookRouter = new BookRouter(new BookServiceImpl(bookDao, new AuthorDaoMapImpl))
    val commentRouter = new CommentRouter(new CommentServiceImpl(bookDao, commentDao))
    val authorRouter = new AuthorRouter(new AuthorServiceImpl(bookDao, commentDao))
    val greetRouter = new GreetRouter(new GreetingServiceImpl(new GreetingDaoImpl))

    val binding =
      Http()
        .newServerAt(host, port)
        .bind(new RouterImpl(Seq(bookRouter, authorRouter, commentRouter, greetRouter)).route)

    binding.foreach(b => println(s"Binding on ${b.localAddress}"))

    StdIn.readLine()

    binding.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}
