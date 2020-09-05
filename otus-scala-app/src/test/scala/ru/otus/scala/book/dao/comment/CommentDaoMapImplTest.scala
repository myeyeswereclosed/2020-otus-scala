package ru.otus.scala.book.dao.comment

class CommentDaoMapImplTest
  extends CommentDaoTest(
    "CommentDaoMapImplTest",
    () => new CommentDaoMapImpl()
  )
