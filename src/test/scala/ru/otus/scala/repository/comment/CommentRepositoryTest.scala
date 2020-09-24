package ru.otus.scala.repository.comment

import java.time.{LocalDateTime, ZonedDateTime}

import org.scalacheck.Arbitrary.{arbitrary, _}
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.{be, _}
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import ru.otus.scala.model.domain.{AppBook, BookComment}
import ru.otus.scala.model.domain.author.Author
import ru.otus.scala.repository.{BookRepository, CommentRepository}

abstract class CommentRepositoryTest(name: String)
  extends AnyFreeSpec
    with ScalaCheckDrivenPropertyChecks with ScalaFutures {
  private val minYearOfPublishing = 1950
  private val maxYearOfPublishing = 2020

  private val minPagesNumber = 5
  private val maxPagesNumber = 1500

  implicit val genBook: Gen[AppBook] = for {
    id <- Gen.option(Gen.uuid)
    title <- arbitrary[String]
    firstName <- Gen.uuid.map(_.toString + "_FirstName")
    lastName <- Gen.uuid.map(_.toString + "_LastName")
    pagesNumber <- Gen.chooseNum(minPagesNumber, maxPagesNumber)
    yearOfPublishing <- Gen.option(Gen.chooseNum(minYearOfPublishing, maxYearOfPublishing))
  } yield
    AppBook(
      id = id,
      title = title,
      authors = Seq(Author(id = None, firstName = firstName, lastName = lastName)),
      pagesNumber = pagesNumber,
      yearOfPublishing = yearOfPublishing
    )

  implicit val arbitraryBook: Arbitrary[AppBook] = Arbitrary(genBook)

  implicit val genComment: Gen[BookComment] = for {
    id <- Gen.option(Gen.uuid)
    text <- Gen.uuid.map(_.toString + "_BookComment")
    book <- arbitrary[AppBook]
  } yield
    BookComment(
      id = id,
      text = text,
      book = book,
      LocalDateTime.now()
    )

  implicit val arbitraryComment: Arbitrary[BookComment] = Arbitrary(genComment)

  def createRepository(): CommentRepository

  def createBookRepository(): Option[BookRepository]

  override implicit def patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(10, Seconds)))

  name - {
    "create" - {
      "create any number of comments" in {
        forAll { (comments: Seq[BookComment], comment: BookComment) =>
          val maybeBookRepository = createBookRepository()
          val repository = createRepository()

          comments
            .map(setupComment(_, maybeBookRepository))
            .map(repository.create)

          val newComment =
            repository.create(setupComment(comment, maybeBookRepository)).futureValue

          newComment.id shouldNot be(comment.id)
          newComment.id shouldNot be(None)

          newComment shouldBe comment.copy(id = newComment.id, book = newComment.book)
        }
      }
    }
  }

  private def setupComment(comment: BookComment, maybeBookRepository: Option[BookRepository]): BookComment = {
      comment.copy(
        book =
          maybeBookRepository
            .map(_.create(comment.book).futureValue)
            .getOrElse(comment.book))
  }
}
