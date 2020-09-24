package ru.otus.scala.greet.dao

trait GreetingDao {
  def message(key: String): Option[String];
}
