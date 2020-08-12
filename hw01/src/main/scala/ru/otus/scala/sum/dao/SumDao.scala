package ru.otus.scala.sum.dao

trait SumDao {
  /**
   * provides sum of ints if they are stored
   *
   * @param first first int
   * @param second second int
   * @return optional sum of ints
   */
  def get(first: Int, second: Int): Option[Int]

  /**
   * stores sum of ints pair
   *
   * @param first first int
   * @param second second int
   * @param sum sum of ints
   */
  def store(first: Int, second: Int, sum: Int): Unit
}
