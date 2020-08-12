package ru.otus.scala.greet.service.impl

import ru.otus.scala.greet.dao.GreetingDao
import ru.otus.scala.greet.model.{GreetRequest, GreetResponse}
import ru.otus.scala.greet.service.GreetingService

class GreetingServiceImpl(dao: GreetingDao) extends GreetingService {
  def greet(request: GreetRequest): GreetResponse =
    if (request.isHuman)
      GreetResponse(s"${dao.greetingPrefix} ${request.name} ${dao.greetingPostfix}")
    else GreetResponse("AAAAAAAAAA!!!!!!")
}
