package ru.otus.scala.repository.author

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.{be, _}
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import ru.otus.scala.model.domain.author.Author
import ru.otus.scala.repository.AuthorRepository

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

  def createRepository(): AuthorRepository

  override implicit def patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(10, Seconds)))

  name - {
    "createAuthor" - {
      "create any number of authors" in {
        forAll { (authors: Seq[Author], author: Author) =>
          val dao = createRepository()

          authors.foreach(dao.create)

          val newAuthor = dao.create(author).futureValue

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

        val dao = createRepository()

        authors1.map(dao.create)

        val expectedAuthor = dao.create(author.copy(firstName = firstName, lastName = lastName)).futureValue

        authors2.map(dao.create)

        dao.findByFirstAndLastName(firstName, lastName).futureValue shouldBe Some(expectedAuthor)
      }
    }
  }
}
