package ru.otus.scala.book.dao.comment

import java.time.ZonedDateTime

import org.scalacheck.Arbitrary.{arbitrary, _}
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.{be, _}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import ru.otus.scala.book.model.domain.Comment.BookComment
import ru.otus.scala.book.model.domain.{Author, Book}

abstract class CommentDaoTest(name: String, createDao: () => CommentDao)
  extends AnyFreeSpec
    with ScalaCheckDrivenPropertyChecks {
  private val minYearOfPublishing = 1950
  private val maxYearOfPublishing = 2020

  private val minPagesNumber = 5
  private val maxPagesNumber = 1500

  implicit val genBook: Gen[Book] = for {
    id <- Gen.option(Gen.uuid)
    title <- arbitrary[String]
    firstName <- arbitrary[String]
    lastName <- arbitrary[String]
    pagesNumber <- Gen.chooseNum(minPagesNumber, maxPagesNumber)
    yearOfPublishing <- Gen.option(Gen.chooseNum(minYearOfPublishing, maxYearOfPublishing))
  } yield
    Book(
      id = id,
      title = title,
      authors = Seq(Author(id = None, firstName = firstName, lastName = lastName)),
      pagesNumber = pagesNumber,
      yearOfPublishing = yearOfPublishing
    )

  implicit val arbitraryBook: Arbitrary[Book] = Arbitrary(genBook)

  implicit val genComment: Gen[BookComment] = for {
    id <- Gen.option(Gen.uuid)
    text <- arbitrary[String]
    book <- arbitrary[Book]
  } yield
    BookComment(
      id = id,
      text = text,
      book = book,
      ZonedDateTime.now()
    )

  implicit val arbitraryComment: Arbitrary[BookComment] = Arbitrary(genComment)

  name - {
    "create" - {
      "create any number of comments" in {
        forAll { (comments: Seq[BookComment], comment: BookComment) =>
          val dao = createDao()

          comments.map(dao.create)

          val newComment = dao.create(comment)

          newComment.id shouldNot be(comment.id)
          newComment.id shouldNot be(None)

          newComment shouldBe comment.copy(id = newComment.id)
        }
      }
    }

    "findAuthorsCommentedMoreThan" in {
      forAll { (comments1: Seq[BookComment], book: Book, comments2: Seq[BookComment]) =>
        val dao = createDao()

        if (comments1.nonEmpty) {
          val commentsToBook = comments1.map(_.copy(book = book))

          commentsToBook.map(dao.create)
          comments2.map(dao.create)

          val authors = dao.findAuthorsCommentedMoreThan(commentsToBook.size - 1)

          authors shouldBe book.authors
        }
      }
    }
  }
}
