package ru.otus.scala.repository.impl.slick

import java.time.LocalDateTime
import java.util.UUID

import ru.otus.scala.repository.impl.{AuthorModel, Book, BookAuthor, Comment}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{TableQuery, Tag}

object Tables {
  class BookTable(tag: Tag) extends Table[Book](tag, "book") {
    val id = column[UUID]("id", O.PrimaryKey, O.AutoInc)
    val title = column[String]("title")
    val pagesNumber = column[Int]("pages_number")
    val yearOfPublishing = column[Option[Int]]("year_of_publishing")

    val * = (id, title, pagesNumber, yearOfPublishing) <> ((Book.apply _).tupled, Book.unapply)
  }

  val books = TableQuery[BookTable]

  class AuthorTable(tag: Tag) extends Table[AuthorModel](tag, "author") {
    val id = column[UUID]("id", O.PrimaryKey, O.AutoInc)
    val firstName = column[String]("first_name")
    val lastName = column[String]("last_name")

    val * = (id, firstName, lastName) <> ((AuthorModel.apply _).tupled, AuthorModel.unapply)
  }

  val authors = TableQuery[AuthorTable]

  class BookAuthorTable(tag: Tag) extends Table[BookAuthor](tag, "book_author") {
    val bookId = column[UUID]("book_id")
    val authorId = column[UUID]("author_id")

    val * = (bookId, authorId) <> ((BookAuthor.apply _).tupled, BookAuthor.unapply)
  }

  val bookAuthors = TableQuery[BookAuthorTable]

  class CommentTable(tag: Tag) extends Table[Comment](tag, "comment") {
    val id = column[UUID]("id")
    val text = column[String]("text")
    val bookId = column[UUID]("book_id")
    val madeAt = column[LocalDateTime]("made_at")

    val * = (id, text, bookId, madeAt) <> ((Comment.apply _).tupled, Comment.unapply)
  }

  val comments = TableQuery[CommentTable]
}
