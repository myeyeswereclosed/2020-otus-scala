package ru.otus.scala.call_count.dao

class CallCountDaoImpl extends CallCountDao {
  var counter = 0L

  def get(): Long = {
    counter
  }

  def increment(): Unit = counter = counter + 1
}
