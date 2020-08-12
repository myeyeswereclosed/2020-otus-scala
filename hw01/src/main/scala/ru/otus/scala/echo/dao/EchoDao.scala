package ru.otus.scala.echo.dao

trait EchoDao {
  /**
   * provides prefix before echoed message
   *
   * @return prefix of echoed message
   */
  def prefix: String
}
