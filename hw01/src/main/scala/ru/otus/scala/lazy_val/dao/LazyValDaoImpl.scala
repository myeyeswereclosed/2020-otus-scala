package ru.otus.scala.lazy_val.dao

class LazyValDaoImpl extends LazyValDao {
  lazy val value: String = {
    println("Printed only once")

    "Hello" + " world"
  }
}
