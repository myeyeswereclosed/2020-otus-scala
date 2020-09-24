package ru.otus.scala.repository.author

import ru.otus.scala.repository.AuthorRepository
import ru.otus.scala.repository.impl.map.AuthorMapRepository

class AuthorMapRepositoryTest extends AuthorRepositoryTest(name = "AuthorMapRepositoryTest") {
  def createRepository(): AuthorRepository =
    new AuthorMapRepository
}
