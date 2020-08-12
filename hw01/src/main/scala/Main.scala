import ru.otus.scala.App
import ru.otus.scala.call_count.model.{CallCountRequest, CallCountResponse}
import ru.otus.scala.echo.model.EchoRequest
import ru.otus.scala.greet.model.GreetRequest
import ru.otus.scala.key_value.model.KeyValueRequest
import ru.otus.scala.lazy_val.model.LazyValRequest
import ru.otus.scala.sum.model.SumRequest

object Main {
  def main(args: Array[String]): Unit = {
    lazy val delimiter = "------------------"
    val app = App()

    println(app.greet(GreetRequest("Jane Doe")))

    println(delimiter)

    println(app.echo(EchoRequest("Hello")))

    println(delimiter)

    println(app.countCalls(CallCountRequest()))
    println(app.countCalls(CallCountRequest()))
    println(app.countCalls(CallCountRequest()))

    println(delimiter)

    println(app.getValue(KeyValueRequest(2)))
    println(app.getValue(KeyValueRequest(0)))

    println(delimiter)

    println(app.getLazyValue(LazyValRequest()))
    println(app.getLazyValue(LazyValRequest()))
    println(app.getLazyValue(LazyValRequest()))

    println(delimiter)

    println(app.sum(SumRequest(2, 6)))
    println(app.sum(SumRequest(6, 2)))
    println(app.sum(SumRequest(2, 6)))
  }
}
