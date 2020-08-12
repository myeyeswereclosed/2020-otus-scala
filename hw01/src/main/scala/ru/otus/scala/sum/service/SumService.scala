package ru.otus.scala.sum.service

import ru.otus.scala.sum.model.{SumRequest, SumResponse}

trait SumService {
  /**
   * provides sum of ints from request
   *
   * @param request request with ints
   * @return response with requested ints sum
   */
  def sum(request: SumRequest): SumResponse
}
