package ru.otus.scala.greet.dao.impl

import ru.otus.scala.greet.dao.GreetingDao

class GreetingDaoImpl extends GreetingDao {
  private val messages: Map[String, String] = Map("prefix" -> "Hi", "postfix" -> "!")

  def message(key: String): Option[String] = messages.get(key)
}
