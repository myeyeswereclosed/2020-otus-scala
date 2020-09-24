package ru.otus.scala.repository.book

import java.lang.StackWalker.Option
import java.util.UUID

import org.scalacheck.Arbitrary._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.concurrent.ScalaFutures
import org.scalacheck.Arbitrary._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.model.domain.author.Author
import ru.otus.scala.repository.BookRepository

/**
 * Abstract test class that should be inherited by tests for any BookRepository implementation
 */
abstract class BookRepositoryTest(name: String)
  extends AnyFreeSpec
    with ScalaCheckDrivenPropertyChecks
    with ScalaFutures {
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

  case class Pages(value: Int)

  implicit val arbitraryPagesNumber: Arbitrary[Pages] =
    Arbitrary(Gen.chooseNum(minPagesNumber, maxPagesNumber).map(Pages))

  def createRepository(): BookRepository

  override implicit def patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(10, Seconds)))

  name - {
    "createBook" - {
      "create any number of books" in {
        forAll { (books: Seq[AppBook], book: AppBook) =>
          val repository = createRepository()

          setupBooks(books, repository)

          val newBook = repository.create(book).futureValue

          newBook.id shouldNot be(book.id)
          newBook.id shouldNot be(None)

          newBook.authors.map(_.id) shouldNot contain(None)
          newBook.authors.map(_.fullName) should be(book.authors.map(_.fullName))

          newBook shouldBe book.copy(id = newBook.id, authors = newBook.authors)
        }
      }
    }
    "getBook" - {
      "find non-stored book" in {
        forAll { (books: Seq[AppBook], bookId: UUID) =>
          val repository = createRepository()

          setupBooks(books, repository)

          repository.findById(bookId).futureValue shouldBe None
        }
      }
      "find stored book" in {
        forAll { (book: AppBook) =>
          val repository = createRepository()

          val createdBook = repository.create(book).futureValue

          repository.findById(createdBook.id.get).futureValue shouldBe Some(createdBook)
        }
      }
    }
    "updateBook" - {
      "update non-stored book - keep all books the same" in {
        forAll { (books: Seq[AppBook], book: AppBook) =>
          val repository = createRepository()
          val createdBooks = setupBooks(books, repository)

          repository.update(book).futureValue shouldBe None

          createdBooks.foreach { book =>
            repository.findById(book.id.get).futureValue shouldBe Some(book)
          }
        }
      }

      "update stored book - keep other books the same" in {
        forAll { (books1: Seq[AppBook], book1: AppBook, book2: AppBook, books2: Seq[AppBook]) =>
          val repository = createRepository()

          val createdBooks1 = setupBooks(books1, repository)
          val createdBook = repository.create(book1).futureValue
          val createdBooks2 = setupBooks(books2, repository)

          val toUpdate = book2.copy(id = createdBook.id)

          val updatedBook = repository.update(toUpdate).futureValue

          updatedBook shouldNot be(None)
          updatedBook.get.authors.map(_.id) shouldNot contain(None)

          repository.findById(toUpdate.id.get).futureValue shouldBe
            Some(toUpdate.copy(authors = updatedBook.get.authors))

          createdBooks1.foreach { book =>
            repository.findById(book.id.get).futureValue shouldBe Some(book)
          }

          createdBooks2.foreach { book =>
            repository.findById(book.id.get).futureValue shouldBe Some(book)
          }
        }
      }
    }

    "deleteBook" - {
      "delete non-stored book - keep all books the same" in {
        forAll { (books: Seq[AppBook], bookId: UUID) =>
          val repository = createRepository()

          val createdBooks = setupBooks(books, repository)

          repository.delete(bookId).futureValue shouldBe None

          createdBooks.foreach { book =>
            repository.findById(book.id.get).futureValue shouldBe Some(book)
          }
        }
      }

      "delete stored book - keep other books the same" in {
        forAll { (books1: Seq[AppBook], book1: AppBook, books2: Seq[AppBook]) =>
          val dao = createRepository()
          val createdBooks1 = setupBooks(books1, dao)
          val createdBook = dao.create(book1).futureValue
          val createdBooks2 = setupBooks(books2, dao)

          dao.findById(createdBook.id.get).futureValue shouldBe Some(createdBook)
          dao.delete(createdBook.id.get).futureValue shouldBe createdBook.id
          dao.findById(createdBook.id.get).futureValue shouldBe None

          createdBooks1.foreach { book =>
            dao.findById(book.id.get).futureValue shouldBe Some(book)
          }

          createdBooks2.foreach { book =>
            dao.findById(book.id.get).futureValue shouldBe Some(book)
          }
        }
      }
    }

    "findBooksByAuthorLastName" in {
      forAll { (books1: Seq[AppBook], firstName: String, lastName: String, books2: Seq[AppBook]) =>
        val repository = createRepository()

        val writtenByAuthorWithAnotherLastName = books1.filterNot(_.authors.map(_.lastName).contains(lastName))
        val writtenByAuthorWithLastName =
          books2.map(
            book =>
              book.copy(
                authors =
                  Author(
                    id = Some(UUID.randomUUID()),
                    firstName = firstName,
                    lastName = lastName
                  ) +: book.authors
              )
          )

        setupBooks(writtenByAuthorWithAnotherLastName, repository)

        val booksWrittenByAuthorWithLastName = setupBooks(writtenByAuthorWithLastName, repository)

        val actualBooksWithAuthorLastName = repository.findAllByAuthorLastName(lastName).futureValue.toSet

        actualBooksWithAuthorLastName.map(_.id.get) shouldBe booksWrittenByAuthorWithLastName.map(_.id.get).toSet
      }
    }

    "findAuthorsPublishedIn" in {
      forAll { (books1: Seq[AppBook], yearOfPublishing: Year, books2: Seq[AppBook]) =>
        val repository = createRepository()

        val writtenInAnotherYear =
          books1.filter(_.yearOfPublishing.isEmpty) ++
            books1.filterNot(_.yearOfPublishing.exists(_ == yearOfPublishing.value))

        val writtenInExpectedYear = books2.map(_.copy(yearOfPublishing = Some(yearOfPublishing.value)))

        setupBooks(writtenInAnotherYear, repository)

        val booksWrittenInExpectedYear = setupBooks(writtenInExpectedYear, repository)
        val expectedAuthors = booksWrittenInExpectedYear.flatMap(_.authors).toSet

        repository.findAuthorsPublishedIn(yearOfPublishing.value).futureValue.toSet shouldBe expectedAuthors
      }
    }

    "findBooksWithPagesNumberGreaterThan" in {
      forAll { (books1: Seq[AppBook], pages: Pages, books2: Seq[AppBook]) =>
        val repository = createRepository()

        val withPagesNumberLessThanExpected = books1.filter(_.pagesNumber < pages.value)
        val withPagesNumberGreaterThanExpected = books2.map(_.copy(pagesNumber = pages.value + 10))

        setupBooks(withPagesNumberLessThanExpected, repository)

        val booksWithPagesNumberGreaterThanExpected = setupBooks(withPagesNumberGreaterThanExpected, repository).toSet

        repository.findAllWithPagesNumberGreaterThan(pages.value).futureValue.toSet shouldBe booksWithPagesNumberGreaterThanExpected
      }
    }

    "findAuthorsWithBooksPagesLessThan" in {
      forAll { (books: Seq[AppBook]) =>
        if (books.nonEmpty) {
          val repository = createRepository()

          val pagesExpected = books.map(_.pagesNumber).sum / books.size

          val withPagesNumberLessThanExpected = books.filter(_.pagesNumber < pagesExpected)
          val withPagesNumberEqualToOrGreaterThanExpected = books.filter(_.pagesNumber >= pagesExpected)

          val nonExpectedAuthors =
            setupBooks(withPagesNumberEqualToOrGreaterThanExpected, repository).flatMap(_.authors).toSet

          val expectedAuthors = setupBooks(withPagesNumberLessThanExpected, repository).flatMap(_.authors).toSet

          repository
            .findAuthorsWithBooksPagesLessThan(
              pagesExpected,
              nonExpectedAuthors ++ expectedAuthors
            ).futureValue.toSet shouldBe expectedAuthors
        }
      }
    }

    "findAll" in {
      forAll { books: Seq[AppBook] =>
        val repository = createRepository()
        val createdBooks = setupBooks(books, repository)

        repository.findAll(0, createdBooks.size).futureValue.toSet shouldBe createdBooks.toSet
      }
    }
  }

  def setupBooks(books: Seq[AppBook], repository: BookRepository): Seq[AppBook] = {
    books.map(repository.create(_).futureValue)
  }

}
