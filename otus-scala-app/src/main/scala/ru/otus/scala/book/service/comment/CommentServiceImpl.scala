package ru.otus.scala.book.service.comment

import ru.otus.scala.book.dao.book.BookDao
import ru.otus.scala.book.dao.comment.CommentDao
import ru.otus.scala.book.model.CreateComment.{BookNotFound, CommentCreated, CreateCommentRequest, CreateCommentResponse}
import ru.otus.scala.book.model.domain.Comment.BookComment

class CommentServiceImpl(bookDao: BookDao, commentDao: CommentDao) extends CommentService {
  def create(request: CreateCommentRequest): CreateCommentResponse =
    bookDao
      .findById(request.bookId)
      .map(book => CommentCreated(commentDao.create(BookComment(id = None, text = request.text, book = book))))
      .getOrElse(BookNotFound(request.bookId))
}
