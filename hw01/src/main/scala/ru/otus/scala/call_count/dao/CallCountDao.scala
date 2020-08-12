package ru.otus.scala.call_count.dao

trait CallCountDao {
  /**
   * increments current counter value
   */
  def increment(): Unit

  /**
   * provides current counter value
   *
   * @return current counter value
   */
  def get(): Long
}
