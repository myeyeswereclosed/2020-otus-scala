package ru.otus.scala.book.dao.book

class BookDaoMapImplTest
  extends BookDaoTest(
    "BookDaoMapImplTest",
    () => new BookDaoMapImpl
  )
