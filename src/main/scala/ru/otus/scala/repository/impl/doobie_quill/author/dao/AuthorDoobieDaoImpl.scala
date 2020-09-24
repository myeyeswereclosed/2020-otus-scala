package ru.otus.scala.repository.impl.doobie_quill.author.dao

import java.util.UUID

import cats.data.NonEmptyList
import cats.instances.list._
import cats.syntax.applicative._
import cats.syntax.apply._
import doobie._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.model.domain.author.{Author, Name}
import ru.otus.scala.repository.impl.doobie_quill.author.AuthorDoobieDao

class AuthorDoobieDaoImpl extends AuthorDoobieDao {
  implicit val putName: Put[(String, String)] = Put[String].contramap(name => s"'${name._1}', '${name._2}'")

  def create(author: Author): ConnectionIO[UUID] =
    sql"""insert into author(first_name, last_name) values (${author.firstName}, ${author.lastName})"""
      .update
      .withGeneratedKeys[UUID]("id")
      .compile
      .lastOrError

  def addAll(authors: Seq[Author]): ConnectionIO[List[Author]] = {
    val query = "insert into author(first_name, last_name) values (?, ?)"

    Update[Name](query)
      .updateManyWithGeneratedKeys[Author]("id", "first_name", "last_name")(authors.toList.map(_.name))
      .compile
      .toList
  }

  def addBookAuthors(book: AppBook, authors: Seq[Author]): ConnectionIO[List[Author]] =
    book
      .id
      .map(bookId =>
        for {
          authors <- addAll(authors)
          _ <- bindAuthorsToBook(authors, bookId)
        } yield authors
      )
      .getOrElse(List[Author]().pure[ConnectionIO])

  def findByFirstAndLastName(firstName: String, lastName: String): ConnectionIO[Option[Author]] =
    sql"""select a.id, a.first_name, a.last_name
         from author a where a.first_name = $firstName and a.last_name = $lastName
    """
    .query[Author]
    .option

  def findByBookId(bookId: UUID): ConnectionIO[List[Author]] =
    sql"""select a.id, a.first_name, a.last_name from author a
         join book_author ba on a.id = ba.author_id
         where ba.book_id = $bookId
       """
    .query[Author]
    .to[List]

  def findAuthorsOf(bookIds: Seq[UUID]): ConnectionIO[Seq[(Author, UUID)]] = {
    val fragment =
      fr"""select a.id, a.first_name, a.last_name, ba.book_id from author a
      join book_author ba on a.id = ba.author_id where
      """

    NonEmptyList
      .fromList(bookIds.toList)
      .map(
        ids =>
          (fragment ++ Fragments.in(fr"ba.book_id", ids))
          .query[(Author, UUID)]
          .to[Seq]
      )
      .getOrElse(Seq[(Author, UUID)]().pure[ConnectionIO])
  }

  def bindAuthorsToBook(authors: Seq[Author], bookId: UUID): ConnectionIO[Int] = {
    val query = "insert into book_author(book_id, author_id) values(?, ?)"

    Update[(UUID, UUID)](query)
      .updateMany(
        authors
          .map(_.id)
          .filter(_.nonEmpty)
          .map(_.get)
          .toList
          .map(id => (bookId, id))
      )
  }

  def publishedIn(year: Int): ConnectionIO[Seq[Author]] =
    sql"""
         select distinct  a.id, a.first_name, a.last_name
         from book b
         join book_author ba on ba.book_id = b.id
         join author a on ba.author_id = a.id
         where b.year_of_publishing = $year
    """
      .query[Author]
      .to[Seq]

  def findAllWithPagesNumberLessThan(pagesNumber: Int, amongAuthors: Set[Author]): ConnectionIO[Seq[Author]] = {
    val fragment =
      fr"""select distinct a.id, a.first_name, a.last_name
         from book b
         join book_author ba on b.id = ba.book_id
         join author a on ba.author_id = a.id
         where pages_number < $pagesNumber and
     """

    NonEmptyList
      .fromList(amongAuthors.filter(_.id.nonEmpty).map(_.id.get).toList)
      .map(
        ids =>
          (fragment ++ Fragments.in(fr"a.id", ids))
            .query[Author]
            .to[Seq]
      )
      .getOrElse(Seq[Author]().pure[ConnectionIO])
  }

  def deleteBookAuthors(authorsIds: Seq[UUID], bookId: UUID): ConnectionIO[Int] = {
    val fragment = fr"""delete from book_author where book_id = $bookId and """

    NonEmptyList
      .fromList(authorsIds.toList)
      .map(
        ids =>
          (fragment ++ Fragments.in(fr"author_id", ids))
            .update
            .run
      )
      .getOrElse(0.pure[ConnectionIO])
  }

  def deleteAll(): ConnectionIO[Int] =
    sql"""delete from book_author""".update.run *> sql"""delete from author""".update.run

  override def findAllPublishedIn(year: Int): ConnectionIO[Seq[Author]] = ???
}
