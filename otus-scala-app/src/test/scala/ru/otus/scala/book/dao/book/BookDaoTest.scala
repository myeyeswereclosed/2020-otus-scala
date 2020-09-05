package ru.otus.scala.book.dao.book

import java.util.UUID

import org.scalacheck.Arbitrary._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import ru.otus.scala.book.model.domain.{Author, Book}

/**
  * Abstract test class that should bw inherited by tests for any BookDao implementation
  */
abstract class BookDaoTest(name: String, createDao: () => BookDao)
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

  case class Year(value: Int)

  implicit val arbitraryYear: Arbitrary[Year] =
    Arbitrary(Gen.chooseNum(minYearOfPublishing, maxYearOfPublishing).map(Year))

  case class Pages(value: Int)

  implicit val arbitraryPagesNumber: Arbitrary[Pages] =
    Arbitrary(Gen.chooseNum(minPagesNumber, maxPagesNumber).map(Pages))

  name - {
    "createBook" - {
      "create any number of books" in {
        forAll { (books: Seq[Book], book: Book) =>
          val dao = createDao()

          setupBooks(books, dao)

          val newBook = dao.create(book)

          newBook.id shouldNot be(book.id)
          newBook.id shouldNot be(None)

          newBook shouldBe book.copy(id = newBook.id)
        }
      }
    }

    "getBook" - {
      "find non-stored book" in {
        forAll { (books: Seq[Book], bookId: UUID) =>
          val dao = createDao()

          setupBooks(books, dao)

          dao.findById(bookId) shouldBe None
        }
      }

      "find stored book" in {
        forAll { (book: Book) =>
          val dao = createDao()

          val createdBook = dao.create(book)

          dao.findById(createdBook.id.get) shouldBe Some(createdBook)
        }
      }
    }

    "updateBook" - {
      "update non-stored book - keep all books the same" in {
        forAll { (books: Seq[Book], book: Book) =>
          val dao = createDao()
          val createdBooks = setupBooks(books, dao)

          dao.update(book) shouldBe None

          createdBooks.foreach { book =>
            dao.findById(book.id.get) shouldBe Some(book)
          }
        }
      }

      "update stored book - keep other books the same" in {
        forAll { (books1: Seq[Book], book1: Book, book2: Book, books2: Seq[Book]) =>
          val dao = createDao()

          val createdBooks1 = setupBooks(books1, dao)
          val createdBook = dao.create(book1)
          val createdBooks2 = setupBooks(books2, dao)

          val toUpdate = book2.copy(id = createdBook.id)

          dao.update(toUpdate) shouldBe Some(toUpdate)
          dao.findById(toUpdate.id.get) shouldBe Some(toUpdate)

          createdBooks1.foreach { book =>
            dao.findById(book.id.get) shouldBe Some(book)
          }

          createdBooks2.foreach { book =>
            dao.findById(book.id.get) shouldBe Some(book)
          }
        }
      }

      "deleteBook" - {
        "delete non-stored book - keep all books the same" in {
          forAll { (books: Seq[Book], bookId: UUID) =>
            val dao = createDao()
            val createdBooks = setupBooks(books, dao)

            dao.delete(bookId) shouldBe None

            createdBooks.foreach { book =>
              dao.findById(book.id.get) shouldBe Some(book)
            }
          }
        }

        "delete stored book - keep other books the same" in {
          forAll { (books1: Seq[Book], book1: Book, books2: Seq[Book]) =>
            val dao = createDao()
            val createdBooks1 = setupBooks(books1, dao)
            val createdBook = dao.create(book1)
            val createdBooks2 = setupBooks(books2, dao)

            dao.findById(createdBook.id.get) shouldBe Some(createdBook)
            dao.delete(createdBook.id.get) shouldBe Some(createdBook)
            dao.findById(createdBook.id.get) shouldBe None

            createdBooks1.foreach { book =>
              dao.findById(book.id.get) shouldBe Some(book)
            }

            createdBooks2.foreach { book =>
              dao.findById(book.id.get) shouldBe Some(book)
            }
          }
        }
      }

      "findBooksByLastName" in {
        forAll { (books1: Seq[Book], lastName: String, books2: Seq[Book]) =>
          val firstName = "StubName"

          val dao = createDao()

          val writtenByAuthorWithAnotherLastName = books1.filterNot(_.authors.map(_.lastName).contains(lastName))
          val writtenByAuthorWithLastName =
            books2.map(
              book =>
                book.copy(
                  authors =
                    Author(id = Some(UUID.randomUUID()), firstName = firstName, lastName = lastName) +: book.authors
                )
            )

          setupBooks(writtenByAuthorWithAnotherLastName, dao)

          val booksWrittenByAuthorWithLastName = setupBooks(writtenByAuthorWithLastName, dao)

          val actualBooksWithAuthorLastName = dao.findAllByAuthorLastName(lastName).toSet

          actualBooksWithAuthorLastName shouldBe booksWrittenByAuthorWithLastName.toSet
          actualBooksWithAuthorLastName
            .map((book: Book) => book.authors.head)
            .foreach(author => author.firstName shouldBe firstName)
        }
      }

      "findAuthorsPublishedIn" in {
        forAll { (books1: Seq[Book], yearOfPublishing: Year, books2: Seq[Book]) =>
            val dao = createDao()

            val writtenInAnotherYear =
              books1.filter(_.yearOfPublishing.isEmpty) ++
              books1.filterNot(_.yearOfPublishing.exists(_ == yearOfPublishing.value))

            val writtenInExpectedYear = books2.map(_.copy(yearOfPublishing = Some(yearOfPublishing.value)))

            setupBooks(writtenInAnotherYear, dao)

            val booksWrittenInExpectedYear = setupBooks(writtenInExpectedYear, dao)
            val expectedAuthors = booksWrittenInExpectedYear.flatMap(_.authors).toSet

            dao.findAuthorsPublishedIn(yearOfPublishing.value).toSet shouldBe expectedAuthors
        }
      }

      "findBooksWithPagesNumberGreaterThan" in {
        forAll { (books1: Seq[Book], pages: Pages, books2: Seq[Book]) =>
          val dao = createDao()

          val withPagesNumberLessThanExpected = books1.filter(_.pagesNumber < pages.value)
          val withPagesNumberGreaterThanExpected = books2.map(_.copy(pagesNumber = pages.value + 10))

          setupBooks(withPagesNumberLessThanExpected, dao)

          val booksWithPagesNumberGreaterThanExpected = setupBooks(withPagesNumberGreaterThanExpected, dao).toSet

          dao.findAllWithPagesNumberGreaterThan(pages.value).toSet shouldBe booksWithPagesNumberGreaterThanExpected
        }
      }

      "findAuthorsWithBooksPagesLessThan" in {
        forAll { (books: Seq[Book]) =>
          if (books.nonEmpty) {
            val dao = createDao()

            val pagesExpected = books.map(_.pagesNumber).sum / books.size

            val withPagesNumberLessThanExpected = books.filter(_.pagesNumber < pagesExpected)
            val withPagesNumberEqualToOrGreaterThanExpected = books.filter(_.pagesNumber >= pagesExpected)

            setupBooks(withPagesNumberEqualToOrGreaterThanExpected, dao)

            val expectedAuthors = setupBooks(withPagesNumberLessThanExpected, dao).flatMap(_.authors).toSet

            dao
              .findAuthorsWithBooksPagesLessThan(
                pagesExpected,
                books.flatMap(_.authors).distinct
              ).toSet shouldBe expectedAuthors
          }
        }
      }

      "findAll" in {
        forAll { books: Seq[Book] =>
          val dao = createDao()
          val createdBooks = books.map(dao.create)

          dao.findAll(0, createdBooks.size).toSet shouldBe createdBooks.toSet
        }
      }
    }
  }

  def setupBooks(books: Seq[Book], dao: BookDao): Seq[Book] = books.map(dao.create)
}
