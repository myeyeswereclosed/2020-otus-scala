package ru.otus.scala.sum.service

import ru.otus.scala.sum.dao.SumDao
import ru.otus.scala.sum.model.{SumRequest, SumResponse}

class SumServiceImpl(dao: SumDao) extends SumService {
  def sum(request: SumRequest): SumResponse = {
    val result =
      dao
        .get(request.first, request.second)
        .orElse(dao.get(request.second, request.first))
        .getOrElse({
          val sum = request.first + request.second

          println(s"Storing sum of ${request.first} and ${request.second}")

          dao.store(request.first, request.second, sum)

          sum
        })

    SumResponse(result)
  }
}
