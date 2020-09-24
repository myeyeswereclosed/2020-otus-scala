package ru.otus.scala.repository.book

import ru.otus.scala.repository.BookRepository
import ru.otus.scala.repository.impl.map.BookMapRepository

class BookMapRepositoryTest extends BookRepositoryTest("BookMapRepositoryTest") {
  def createRepository(): BookRepository = new BookMapRepository
}
