package ru.otus.scala.book.service.author

import ru.otus.scala.book.dao.book.BookDao
import ru.otus.scala.book.dao.comment.CommentDao
import ru.otus.scala.book.model.AuthorsByYearOfPublishing.{AuthorsByYearOfPublishingRequest, AuthorsByYearOfPublishingResponse}
import ru.otus.scala.book.model.domain.Author

class AuthorServiceImpl(bookDao: BookDao, commentDao: CommentDao) extends AuthorService {
  def getAllPublishedIn(request: AuthorsByYearOfPublishingRequest): AuthorsByYearOfPublishingResponse =
    AuthorsByYearOfPublishingResponse(bookDao.findAuthorsPublishedIn(request.year))

  def getAllCommentedMoreThan(commentsNumber: Int): Seq[Author] =
    commentDao.findAuthorsCommentedMoreThan(commentsNumber)
}
