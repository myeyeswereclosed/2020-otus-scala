package ru.otus.scala

import ru.otus.scala.call_count.dao.{CallCountDao, CallCountDaoImpl}
import ru.otus.scala.call_count.model.{CallCountRequest, CallCountResponse}
import ru.otus.scala.call_count.service.{CallCountService, CallCountServiceImpl}
import ru.otus.scala.echo.dao.EchoDaoImpl
import ru.otus.scala.echo.model.{EchoRequest, EchoResponse}
import ru.otus.scala.echo.service.{EchoService, EchoServiceImpl}
import ru.otus.scala.greet.dao.impl.GreetingDaoImpl
import ru.otus.scala.greet.model.{GreetRequest, GreetResponse}
import ru.otus.scala.greet.service.GreetingService
import ru.otus.scala.greet.service.impl.GreetingServiceImpl
import ru.otus.scala.key_value.dao.KeyValueDaoImpl
import ru.otus.scala.key_value.model.{KeyValueRequest, KeyValueResponse}
import ru.otus.scala.key_value.service.{KeyValueService, KeyValueServiceImpl}
import ru.otus.scala.lazy_val.dao.LazyValDaoImpl
import ru.otus.scala.lazy_val.model.{LazyValRequest, LazyValResponse}
import ru.otus.scala.lazy_val.service.{LazyValService, LazyValServiceImpl}
import ru.otus.scala.sum.dao.SumDaoImpl
import ru.otus.scala.sum.model.{SumRequest, SumResponse}
import ru.otus.scala.sum.service.{SumService, SumServiceImpl}

trait App {
  def greet(request: GreetRequest): GreetResponse

  def echo(request: EchoRequest): EchoResponse

  def countCalls(request: CallCountRequest): CallCountResponse

  def getValue(request: KeyValueRequest): KeyValueResponse

  def getLazyValue(request: LazyValRequest): LazyValResponse

  def sum(request: SumRequest): SumResponse
}

object App {
  private class AppImpl(
    greeting: GreetingService,
    echoService: EchoService,
    callCountService: CallCountService,
    keyValueService: KeyValueService,
    lazyValService: LazyValService,
    sumService: SumService
  ) extends App {
    def greet(request: GreetRequest): GreetResponse = greeting.greet(request)

    def echo(request: EchoRequest): EchoResponse = echoService.echo(request)

    def countCalls(request: CallCountRequest): CallCountResponse = callCountService.count(request)

    def getValue(request: KeyValueRequest): KeyValueResponse = keyValueService.getValue(request)

    def getLazyValue(request: LazyValRequest): LazyValResponse = lazyValService.get(request)

    def sum(request: SumRequest): SumResponse = sumService.sum(request)
  }

  def apply(): App = {
    val greetingDao     = new GreetingDaoImpl
    val greetingService = new GreetingServiceImpl(greetingDao)

    val echoDao = new EchoDaoImpl
    val echoService = new EchoServiceImpl(echoDao)

    val callCountDao = new CallCountDaoImpl
    val callCountService = new CallCountServiceImpl(callCountDao)

    val keyValueDao =
      new KeyValueDaoImpl(
        Map(
          1 -> "first",
          2 -> "second",
          3 -> "third",
        )
      )
    val keyValueService = new KeyValueServiceImpl(keyValueDao)

    val lazyValDao = new LazyValDaoImpl
    val lazyValService = new LazyValServiceImpl(lazyValDao)

    val sumDao = new SumDaoImpl
    val sumService = new SumServiceImpl(sumDao)

    new AppImpl(greetingService, echoService, callCountService, keyValueService, lazyValService, sumService)
  }
}
