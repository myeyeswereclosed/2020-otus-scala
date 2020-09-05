package ru.otus.scala.greet.service.impl

import ru.otus.scala.greet.dao.GreetingDao
import ru.otus.scala.greet.model.{GreetRequest, GreetResponse}
import ru.otus.scala.greet.service.GreetingService

class GreetingServiceImpl(dao: GreetingDao) extends GreetingService {
  def greet(request: GreetRequest): GreetResponse =
    if (request.isHuman)
      GreetResponse(
        s"${messageOrEmpty("prefix")} ${request.name} ${messageOrEmpty("postfix")}"
      )
    else
      GreetResponse("AAAAAAAAAA!!!!!!")

  private def messageOrEmpty(key: String): String = {
    dao.message(key).getOrElse("")
  }
}
