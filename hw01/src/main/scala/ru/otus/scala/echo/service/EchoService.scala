package ru.otus.scala.echo.service

import ru.otus.scala.echo.model.{EchoRequest, EchoResponse}

trait EchoService {
  /**
   * Echoes request content if it's not empty or provides some default message
   *
   * @param request request with some message
   * @return response with the same message or default one
   */
  def echo(request: EchoRequest): EchoResponse
}
