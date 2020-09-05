package ru.otus.scala.book.dao.author

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.{be, _}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import ru.otus.scala.book.model.domain.Author

class AuthorDaoTest (name: String, createDao: () => AuthorDao)
  extends AnyFreeSpec
    with ScalaCheckDrivenPropertyChecks {

  implicit val genAuthor: Gen[Author] = for {
    id <- Gen.option(Gen.uuid)
    firstName <- arbitrary[String]
    lastName <- arbitrary[String]
  } yield Author(id, firstName, lastName)

  implicit val arbitraryAuthor: Arbitrary[Author] = Arbitrary(genAuthor)

  name - {
    "createAuthor" - {
      "create any number of authors" in {
        forAll { (authors: Seq[Author], author: Author) =>
          val dao = createDao()

          authors.foreach(dao.create)

          val newAuthor = dao.create(author)

          newAuthor.id shouldNot be(author.id)
          newAuthor.id shouldNot be(None)

          newAuthor shouldBe author.copy(id = newAuthor.id)
        }
      }
    }

    "findByFirstAndLastName" in {
      forAll { (authors1: Seq[Author], author: Author, authors2: Seq[Author]) =>
        val firstName = "MyFirstName"
        val lastName = "MyLastName"

        val dao = createDao()

        authors1.map(dao.create)

        val expectedAuthor = dao.create(author.copy(firstName = firstName, lastName = lastName))

        authors2.map(dao.create)

        dao.findByFirstAndLastName(firstName, lastName) shouldBe Some(expectedAuthor)
      }
    }

    "addAll" in {
      forAll { (authors1: Seq[Author], authors2: Seq[Author]) =>
        val dao = createDao()

        authors1.map(dao.create)

        val addedOnes = dao.addAll(authors2.map(_.copy(id = None)))

        addedOnes.size shouldBe authors2.size
        addedOnes.foreach(_.id shouldNot be(None))
      }
    }
  }
}
