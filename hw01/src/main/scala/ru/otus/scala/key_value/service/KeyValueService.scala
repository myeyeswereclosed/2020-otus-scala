package ru.otus.scala.key_value.service

import ru.otus.scala.key_value.model.{KeyValueRequest, KeyValueResponse}

trait KeyValueService {
  /**
   * provides response with value stored by key in request or message that value is not found
   *
   * @param request request with some key
   * @return response with stored value or some default message
   */
  def getValue(request: KeyValueRequest): KeyValueResponse
}
