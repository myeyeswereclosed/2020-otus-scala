package ru.otus.scala.key_value.dao

trait KeyValueDao {
  /**
   * provides value if it is stored by key
   *
   * @param key key of value
   * @return optional value stored by key
   */
  def get(key: Int): Option[String]
}
