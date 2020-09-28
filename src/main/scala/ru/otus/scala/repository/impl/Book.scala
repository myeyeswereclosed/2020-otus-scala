package ru.otus.scala.repository.impl

import java.util.UUID

import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.model.domain.AppAuthor.Author

case class Book(
  id: UUID,
  title:String,
  pagesNumber: Int,
  yearOfPublishing: Option[Int]
) {
  def toBook(authors: Seq[Author]): AppBook =
    AppBook(
      id = Some(id),
      title = title,
      authors = authors,
      pagesNumber = pagesNumber,
      yearOfPublishing = yearOfPublishing
    )
}

object Book {
  def fromBook(book: AppBook): Book =
    Book(
      book.id.getOrElse(UUID.randomUUID()),
      book.title,
      book.pagesNumber,
      book.yearOfPublishing
    )
}
