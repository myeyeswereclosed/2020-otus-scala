package ru.otus.scala.echo.model

case class EchoRequest(content: String) {
  val isEmpty: Boolean = content.trim == ""
}
