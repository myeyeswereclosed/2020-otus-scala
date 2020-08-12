package ru.otus.scala.lazy_val.dao

trait LazyValDao {
  /**
   * provides lazy value
   *
   * @return some lazy value
   */
  def value(): String
}
