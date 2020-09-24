package ru.otus.scala.repository.impl.doobie_quill.book

import java.util.UUID

import doobie.free.connection.ConnectionIO
import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.repository.dao.BookDao
import ru.otus.scala.repository.impl.Book

trait BookDoobieDao extends BookDao[ConnectionIO] {
  def save(book: AppBook): ConnectionIO[UUID]

  def findById(id: UUID): ConnectionIO[Option[Book]]

  def findByIdForUpdate(id: UUID): ConnectionIO[Option[Book]]

  def findAll(page: Int, size: Int): ConnectionIO[Seq[Book]]

  def update(book: AppBook): ConnectionIO[AppBook]

  def delete(id: UUID): ConnectionIO[Option[UUID]]

  def findAllByAuthorLastName(lastName: String): ConnectionIO[Seq[Book]]

  def findAllWithPagesNumberGreaterThan(pagesNumber: Int): ConnectionIO[Seq[Book]]

  def deleteAll(): ConnectionIO[Int]
}
