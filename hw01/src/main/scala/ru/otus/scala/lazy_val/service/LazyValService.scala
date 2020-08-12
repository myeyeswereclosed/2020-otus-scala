package ru.otus.scala.lazy_val.service

import ru.otus.scala.lazy_val.model.{LazyValRequest, LazyValResponse}

trait LazyValService {
  /**
   * provides response with lazy value for request made
   *
   * @param request request made
   * @return response with some lazy value
   */
  def get(request: LazyValRequest): LazyValResponse
}
