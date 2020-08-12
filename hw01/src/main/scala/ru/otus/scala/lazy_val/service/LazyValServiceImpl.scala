package ru.otus.scala.lazy_val.service

import ru.otus.scala.lazy_val.dao.LazyValDao
import ru.otus.scala.lazy_val.model.{LazyValRequest, LazyValResponse}

class LazyValServiceImpl(dao: LazyValDao) extends LazyValService {
  def get(request: LazyValRequest): LazyValResponse = LazyValResponse(dao.value())
}