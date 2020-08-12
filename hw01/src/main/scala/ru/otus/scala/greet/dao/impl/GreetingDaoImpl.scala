package ru.otus.scala.greet.dao.impl

import ru.otus.scala.greet.dao.GreetingDao

class GreetingDaoImpl extends GreetingDao {
  val greetingPrefix: String  = "Hi"
  val greetingPostfix: String = "!"
}
