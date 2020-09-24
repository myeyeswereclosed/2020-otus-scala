package ru.otus.scala.service.book

import ru.otus.scala.model.AddBookAuthor.AddAuthorResponse.AuthorAdded
import ru.otus.scala.model.AddBookAuthor.{AddAuthorRequest, AddAuthorResponse}
import ru.otus.scala.model.AllBooks.{AllBooksRequest, AllBooksResponse}
import ru.otus.scala.model.BooksByAuthorLastName.{BooksByAuthorLastNameRequest, BooksByAuthorLastNameResponse}
import ru.otus.scala.model.CreateBook.{BookCreated, CreateBookRequest}
import ru.otus.scala.model.DeleteBook.DeleteBookResponse.BookDeleted
import ru.otus.scala.model.DeleteBook.{DeleteBookRequest, DeleteBookResponse}
import ru.otus.scala.model.GetBook.GetBookResponse.{BookFound, BookNotFound}
import ru.otus.scala.model.GetBook.{GetBookRequest, GetBookResponse}
import ru.otus.scala.model.UpdateBook.UpdateBookResponse.{BookUpdated, CantUpdateBookWithoutId}
import ru.otus.scala.model.UpdateBook.{UpdateBookRequest, UpdateBookResponse}
import ru.otus.scala.model.AddBookAuthor.AddAuthorResponse.AuthorAdded
import ru.otus.scala.model.AddBookAuthor.{AddAuthorRequest, AddAuthorResponse}
import ru.otus.scala.model.AllBooks.{AllBooksRequest, AllBooksResponse}
import ru.otus.scala.model.BooksByAuthorLastName.{BooksByAuthorLastNameRequest, BooksByAuthorLastNameResponse}
import ru.otus.scala.model.CreateBook.{BookCreated, CreateBookRequest}
import ru.otus.scala.model.DeleteBook.DeleteBookResponse.BookDeleted
import ru.otus.scala.model.DeleteBook.{DeleteBookRequest, DeleteBookResponse}
import ru.otus.scala.model.GetBook.GetBookResponse.BookFound
import ru.otus.scala.model.GetBook.{GetBookRequest, GetBookResponse}
import ru.otus.scala.model.UpdateBook.UpdateBookResponse.CantUpdateBookWithoutId
import ru.otus.scala.model.UpdateBook.{UpdateBookRequest, UpdateBookResponse}
import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.model.domain.author.Author
import ru.otus.scala.repository.{AuthorRepository, BookRepository}

import scala.concurrent.{ExecutionContext, Future}

class BookServiceImpl(
 bookRepository: BookRepository,
 authorRepository: AuthorRepository
)(implicit ec: ExecutionContext) extends BookService {

  def create(request: CreateBookRequest): Future[BookCreated] = {
    bookRepository.create(request.book).map(BookCreated)
  }

  def getAll(request: AllBooksRequest): Future[AllBooksResponse] =
    bookRepository.findAll(request.page, request.size).map(AllBooksResponse)

  def get(request: GetBookRequest): Future[GetBookResponse] =
    bookRepository
      .findById(request.id)
      .map(_.map(BookFound).getOrElse(BookNotFound(request.id)))

  def update(request: UpdateBookRequest): Future[UpdateBookResponse] =
    request.book.id match {
      case Some(id) =>
        bookRepository.update(request.book).map {
          case Some(book) => BookUpdated(book)
          case None => UpdateBookResponse.BookNotFound(id)
        }
      case None => Future.successful(CantUpdateBookWithoutId)
    }

  def delete(request: DeleteBookRequest): Future[DeleteBookResponse] =
    bookRepository
      .delete(request.id)
      .map(
        _
          .map(BookDeleted)
          .getOrElse(DeleteBookResponse.BookNotFound(request.id))
      )

  def addAuthor(request: AddAuthorRequest): Future[AddAuthorResponse] =
    (for {
      book <- bookRepository.findById(request.bookId)
      bookUpdated <- book.map(addBookAuthor(_, request.author)).getOrElse(Future.successful(None))
    } yield bookUpdated)
      .map(
        _
          .map(AuthorAdded)
          .getOrElse(AddAuthorResponse.BookNotFound(request.bookId))
      )

  def getAllByAuthorLastName(request: BooksByAuthorLastNameRequest): Future[BooksByAuthorLastNameResponse] =
      bookRepository
        .findAllByAuthorLastName(request.lastName)
        .map(BooksByAuthorLastNameResponse)

  def booksByPagesNumberGreaterThanAndAuthorsWithPagesNumberLessThan(
    pagesNumberFrom: Int,
    otherAuthorBookPagesNumberTo: Int
  ): Future[Seq[AppBook]] = {
    for {
      booksWithPagesGreaterThan <- bookRepository.findAllWithPagesNumberGreaterThan(pagesNumberFrom)
      authorsWithBooksPagesLessThan <- bookRepository
        .findAuthorsWithBooksPagesLessThan(
          otherAuthorBookPagesNumberTo,
          booksWithPagesGreaterThan.flatMap(_.authors.toSeq).toSet
        )
    } yield
      booksWithPagesGreaterThan.filter(_.authors.intersect(authorsWithBooksPagesLessThan).nonEmpty)
  }

  private def addBookAuthor(book: AppBook, author: Author): Future[Option[AppBook]] =
    if (book.isWrittenBy(author))
      Future.successful(Some(book))
    else
      for {
        maybeAuthor <- authorRepository.findByFirstAndLastName(author.firstName, author.lastName)
        book <- maybeAuthor
          .map(addAuthor(book, _))
          .getOrElse(authorRepository.create(author).flatMap(addAuthor(book, _)))
      } yield book

  private def addAuthor(book: AppBook, author: Author): Future[Option[AppBook]] =
    bookRepository.update(book.copy(authors = (author +: book.authors).reverse))
}
