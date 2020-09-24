package ru.otus.scala.model.domain.author

import java.util.UUID


case class Author(id: Option[UUID], firstName: String, lastName: String) {
  def hasFirstAndLastName(firstName: String, lastName: String): Boolean =
    hasFirstName(firstName) && hasLastName(lastName)

  def hasFirstName(name: String): Boolean = firstName == name

  def hasLastName(name: String): Boolean = lastName == name

  val fullName: String = firstName.trim + " " + lastName.trim

  val name: Name = Name(FirstName(firstName), LastName(lastName))
}

case class FirstName(value: String)
case class LastName(value: String)

case class Name(firstName: FirstName, lastName: LastName)
