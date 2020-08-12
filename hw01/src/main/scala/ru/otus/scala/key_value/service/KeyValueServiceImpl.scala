package ru.otus.scala.key_value.service

import ru.otus.scala.key_value.dao.KeyValueDao
import ru.otus.scala.key_value.model.{KeyValueRequest, KeyValueResponse}

class KeyValueServiceImpl(dao: KeyValueDao) extends KeyValueService {
  def getValue(request: KeyValueRequest): KeyValueResponse =
    dao
      .get(request.key)
      .map(KeyValueResponse.apply)
      .getOrElse(KeyValueResponse("Value not found"))
}
