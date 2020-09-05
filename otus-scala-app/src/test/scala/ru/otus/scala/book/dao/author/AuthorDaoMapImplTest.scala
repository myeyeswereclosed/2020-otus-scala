package ru.otus.scala.book.dao.author

class AuthorDaoMapImplTest
  extends AuthorDaoTest(
    name = "AuthorDaoMapImplTest",
    () => new AuthorDaoMapImpl
  )
