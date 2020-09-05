package ru.otus.scala.book.service.book

import ru.otus.scala.book.dao.author.AuthorDao
import ru.otus.scala.book.dao.book.BookDao
import ru.otus.scala.book.model.AddBookAuthor.AddAuthorResponse.AuthorAdded
import ru.otus.scala.book.model.AddBookAuthor.{AddAuthorRequest, AddAuthorResponse}
import ru.otus.scala.book.model.AllBooks.{AllBooksRequest, AllBooksResponse}
import ru.otus.scala.book.model.BooksByAuthorLastName.{BooksByAuthorLastNameRequest, BooksByAuthorLastNameResponse}
import ru.otus.scala.book.model.CreateBook.{BookCreated, CreateBookRequest}
import ru.otus.scala.book.model.DeleteBook.DeleteBookResponse.BookDeleted
import ru.otus.scala.book.model.DeleteBook.{DeleteBookRequest, DeleteBookResponse}
import ru.otus.scala.book.model.GetBook.GetBookResponse.{BookFound, BookNotFound}
import ru.otus.scala.book.model.GetBook.{GetBookRequest, GetBookResponse}
import ru.otus.scala.book.model.UpdateBook.UpdateBookResponse.{BookUpdated, CantUpdateBookWithoutId}
import ru.otus.scala.book.model.UpdateBook.{UpdateBookRequest, UpdateBookResponse}
import ru.otus.scala.book.model.domain.{Author, Book}

class BookServiceImpl(bookDao: BookDao, authorDao: AuthorDao) extends BookService {
  def create(request: CreateBookRequest): BookCreated = {
    val authors = request.book.authors

    if (authors.isEmpty)
      BookCreated(bookDao.create(request.book))
    else
      BookCreated(bookDao.create(request.book.copy(authors = authorDao.addAll(authors))))
  }

  def getAll(request: AllBooksRequest): AllBooksResponse = AllBooksResponse(bookDao.findAll(request.page, request.size))

  def get(request: GetBookRequest): GetBookResponse =
    bookDao
      .findById(request.id)
      .map(BookFound)
      .getOrElse(BookNotFound(request.id))

  def update(request: UpdateBookRequest): UpdateBookResponse =
    request.book.id match {
      case Some(id) =>
        bookDao.update(request.book) match {
          case Some(book) => BookUpdated(book)
          case None => UpdateBookResponse.BookNotFound(id)
        }
      case None => CantUpdateBookWithoutId
    }

  def delete(request: DeleteBookRequest): DeleteBookResponse =
    bookDao
      .delete(request.id)
      .map(BookDeleted)
      .getOrElse(DeleteBookResponse.BookNotFound(request.id))

  def addAuthor(request: AddAuthorRequest): AddAuthorResponse =
    (for {
      book <- bookDao.findById(request.bookId)
      bookUpdated <- addBookAuthor(book, request.author)
    } yield bookUpdated)
      .map(AuthorAdded)
      .getOrElse(AddAuthorResponse.BookNotFound(request.bookId))

  def getAllByAuthorLastName(request: BooksByAuthorLastNameRequest): BooksByAuthorLastNameResponse =
    BooksByAuthorLastNameResponse(bookDao.findAllByAuthorLastName(request.lastName))

  def booksByPagesNumberGreaterThanAndAuthorsWithPagesNumberLessThan(
    pagesNumberFrom: Int,
    otherAuthorBookPagesNumberTo: Int
  ): Seq[Book] = {
    val booksWithPagesGreaterThan = bookDao.findAllWithPagesNumberGreaterThan(pagesNumberFrom)

    val authorsWithBooksPagesLessThan =
      bookDao
        .findAuthorsWithBooksPagesLessThan(
          otherAuthorBookPagesNumberTo,
          booksWithPagesGreaterThan.flatMap(_.authors.toSeq)
        )

    booksWithPagesGreaterThan.filter(_.authors.intersect(authorsWithBooksPagesLessThan).nonEmpty)
  }

  private def addBookAuthor(book: Book, author: Author): Option[Book] =
    if (book.isWrittenBy(author))
      Some(book)
    else
      authorDao
        .findByFirstAndLastName(author.firstName, author.lastName)
        .map(existingAuthor => addAuthor(book, existingAuthor))
        .getOrElse(addAuthor(book, authorDao.create(author)))

  private def addAuthor(book: Book, author: Author): Option[Book] =
    bookDao.update(book.copy(authors = (author +: book.authors).reverse))
}
