package ru.otus.scala.repository.author

import ru.otus.scala.repository.{AuthorRepository, BookRepository}
import ru.otus.scala.repository.impl.map.{AuthorMapRepository, BookMapRepository}

class AuthorMapRepositoryTest extends AuthorRepositoryTest(name = "AuthorMapRepositoryTest") {

  def createRepositories(): (AuthorRepository, BookRepository) = {
    val bookRepository = new BookMapRepository
    (new AuthorMapRepository(bookRepository), bookRepository)
  }

}
