package ru.otus.scala.greet.service

import ru.otus.scala.greet.model.{GreetRequest, GreetResponse}

trait GreetingService {
  /**
   * provides some greeting according to request made
   *
   * @param request request made
   * @return response with greeting
   */
  def greet(request: GreetRequest): GreetResponse
}
