package ru.otus.scala.echo.service

import ru.otus.scala.echo.dao.EchoDao
import ru.otus.scala.echo.model.{EchoRequest, EchoResponse}

class EchoServiceImpl(dao: EchoDao) extends EchoService {
  def echo(request: EchoRequest): EchoResponse = {
    if (request.isEmpty)
      EchoResponse("Tell me something!")
    else
      EchoResponse(dao.prefix + request.content)
  }
}
