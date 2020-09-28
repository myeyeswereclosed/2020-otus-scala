package ru.otus.scala.model.domain

import java.util.UUID

import AppAuthor.Author

case class AppBook(
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
