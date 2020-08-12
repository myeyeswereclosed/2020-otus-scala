package ru.otus.scala.sum.dao

import scala.collection.mutable

class SumDaoImpl extends SumDao {
  var storage: mutable.Map[(Int, Int), Int] = mutable.Map[(Int, Int), Int]()

  def get(first: Int, second: Int): Option[Int] =
    storage.get(first, second)

  def store(first: Int, second: Int, sum: Int): Unit = {
    storage = storage.addOne((first, second), sum)
  }
}
