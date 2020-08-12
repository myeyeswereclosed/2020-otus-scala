package ru.otus.scala.call_count.service

import ru.otus.scala.call_count.model.{CallCountRequest, CallCountResponse}

trait CallCountService {
  /**
   * counts number of method calls
   *
   * @param request incoming request
   * @return response containing number of calls
   */
  def count(request: CallCountRequest): CallCountResponse
}
