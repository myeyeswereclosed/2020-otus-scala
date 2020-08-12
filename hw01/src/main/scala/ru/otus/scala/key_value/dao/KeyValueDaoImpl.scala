package ru.otus.scala.key_value.dao

class KeyValueDaoImpl(storage: Map[Int, String]) extends KeyValueDao {
  def get(key: Int): Option[String] = storage.get(key)
}
