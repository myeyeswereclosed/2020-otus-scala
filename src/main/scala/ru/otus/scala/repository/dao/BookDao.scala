package ru.otus.scala.repository.dao

import java.util.UUID

import ru.otus.scala.model.domain.AppBook
import ru.otus.scala.repository.impl.Book

trait BookDao[F[_]] {
  def save(book: AppBook): F[UUID]

  def findById(id: UUID): F[Option[Book]]

  def findByIdForUpdate(id: UUID): F[Option[Book]]

  def findAll(page: Int, size: Int): F[Seq[Book]]

  def update(book: AppBook): F[AppBook]

  def delete(id: UUID): F[Option[UUID]]

  def findAllByAuthorLastName(lastName: String): F[Seq[Book]]

  def findAllWithPagesNumberGreaterThan(pagesNumber: Int): F[Seq[Book]]

  def deleteAll(): F[Int]
}
