package ru.otus.scala.greet.dao

trait GreetingDao {
  /**
   * provides greeting prefix
   *
   * @return greeting prefix
   */
  def greetingPrefix: String

  /**
   * provides greeting postfix
   *
   * @return greeting postfix
   */
  def greetingPostfix: String
}
