package ru.otus.scala.book.model.domain

import java.util.UUID

case class Book(
                 id: Option[UUID],
                 title:String,
                 authors: Seq[Author],
                 pagesNumber: Int,
                 yearOfPublishing: Option[Int]
               ) {
  def isWrittenBy(author: Author): Boolean =
    authors.exists(bookAuthor => bookAuthor.hasFirstAndLastName(author.firstName, author.lastName))

  def isPublishedIn(year: Int): Boolean = yearOfPublishing.contains(year)
}
