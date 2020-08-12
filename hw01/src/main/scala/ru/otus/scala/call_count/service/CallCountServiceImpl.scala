package ru.otus.scala.call_count.service

import ru.otus.scala.call_count.dao.CallCountDao
import ru.otus.scala.call_count.model.{CallCountRequest, CallCountResponse}

class CallCountServiceImpl(dao: CallCountDao) extends CallCountService {
  def count(request: CallCountRequest): CallCountResponse = {
    dao.increment()
    CallCountResponse(dao.get())
  }
}
