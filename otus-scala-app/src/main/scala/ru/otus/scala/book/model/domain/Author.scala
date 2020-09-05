package ru.otus.scala.book.model.domain

import java.util.UUID

case class Author(id: Option[UUID], firstName: String, lastName: String) {
  def hasFirstAndLastName(firstName: String, lastName: String): Boolean =
    hasFirstName(firstName) && hasLastName(lastName)

  def hasFirstName(name: String): Boolean = firstName == name

  def hasLastName(name: String): Boolean = lastName == name
}
