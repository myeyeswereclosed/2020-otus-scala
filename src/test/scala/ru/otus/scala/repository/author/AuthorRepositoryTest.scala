package ru.otus.scala.repository.author

import java.util.UUID

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.{be, _}
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.model.domain.AppAuthor.Author
import ru.otus.scala.repository.{AuthorRepository, BookRepository}

abstract class AuthorRepositoryTest(name: String)
  extends AnyFreeSpec
    with ScalaCheckDrivenPropertyChecks
    with ScalaFutures {

  implicit val genAuthor: Gen[Author] = for {
    id <- Gen.option(Gen.uuid)
    firstName <- arbitrary[String]
    lastName <- arbitrary[String]
  } yield Author(id, firstName, lastName)

  implicit val arbitraryAuthor: Arbitrary[Author] = Arbitrary(genAuthor)

  private val minYearOfPublishing = 1950
  private val maxYearOfPublishing = 2020

  private val minPagesNumber = 5
  private val maxPagesNumber = 1500

  implicit val genBook: Gen[AppBook] = for {
    id <- Gen.option(Gen.uuid)
    title <- Gen.alphaLowerStr
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

  case class Year(value: Int)

  implicit val arbitraryYear: Arbitrary[Year] =
    Arbitrary(Gen.chooseNum(minYearOfPublishing, maxYearOfPublishing).map(Year))

  def createRepositories(): (AuthorRepository, BookRepository)

  override implicit def patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(10, Seconds)))

  name - {
    "createAuthor" - {
      "create any number of authors" in {
        forAll { (authors: Seq[Author], author: Author) =>
          val repository = createRepositories()._1

          authors.foreach(repository.create)

          val newAuthor = repository.create(author).futureValue

          newAuthor.id shouldNot be(author.id)
          newAuthor.id shouldNot be(None)

          newAuthor shouldBe author.copy(id = newAuthor.id)
        }
      }
    }

    "findByFirstAndLastName" in {
      forAll { (authors1: Seq[Author], id1: UUID, id2: UUID, author: Author, authors2: Seq[Author]) =>
        val firstName = id1.toString + "_MyFirstName"
        val lastName = id2.toString + "_MyLastName"

        val repository = createRepositories()._1

        authors1.map(repository.create)

        val expectedAuthor =
          repository
            .create(author.copy(firstName = firstName, lastName = lastName))
            .futureValue

        authors2.map(repository.create)

        repository.findByFirstAndLastName(firstName, lastName).futureValue shouldBe Some(expectedAuthor)
      }
    }

    "findAuthorsPublishedIn" in {
      forAll { (books1: Seq[AppBook], yearOfPublishing: Year, books2: Seq[AppBook]) =>
        val repositories = createRepositories()
        val repository = repositories._1
        val bookRepository = repositories._2

        val writtenInAnotherYear =
          books1.filter(_.yearOfPublishing.isEmpty) ++
            books1.filterNot(_.yearOfPublishing.exists(_ == yearOfPublishing.value))

        val writtenInExpectedYear = books2.map(_.copy(yearOfPublishing = Some(yearOfPublishing.value)))

        setupBooks(writtenInAnotherYear, bookRepository)

        val booksWrittenInExpectedYear = setupBooks(writtenInExpectedYear, bookRepository)
        val expectedAuthors = booksWrittenInExpectedYear.flatMap(_.authors).toSet

        repository.findPublishedIn(yearOfPublishing.value).futureValue.toSet shouldBe expectedAuthors
      }
    }

    "findAuthorsWithBooksPagesLessThan" in {
      forAll { (books: Seq[AppBook]) =>
        if (books.nonEmpty) {
          val repositories = createRepositories()
          val repository = repositories._1
          val bookRepository = repositories._2

          val pagesExpected = books.map(_.pagesNumber).sum / books.size

          val withPagesNumberLessThanExpected = books.filter(_.pagesNumber < pagesExpected)
          val withPagesNumberEqualToOrGreaterThanExpected = books.filter(_.pagesNumber >= pagesExpected)

          val nonExpectedAuthors =
            setupBooks(withPagesNumberEqualToOrGreaterThanExpected, bookRepository).flatMap(_.authors).toSet

          val expectedAuthors = setupBooks(withPagesNumberLessThanExpected, bookRepository).flatMap(_.authors).toSet

          repository
            .findAuthorsWithBooksPagesLessThan(
              pagesExpected,
              nonExpectedAuthors ++ expectedAuthors
            ).futureValue.toSet shouldBe expectedAuthors
        }
      }
    }
  }

  def setupBooks(books: Seq[AppBook], repository: BookRepository): Seq[AppBook] = {
    books.map(repository.create(_).futureValue)
  }
}
