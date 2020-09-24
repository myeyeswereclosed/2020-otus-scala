package ru.otus.scala.repository.impl.slick

import slick.dbio.{DBIOAction, Effect, NoStream}

object DbAction {
  type DbAction[T] = DBIOAction[T, NoStream, Effect.All]

  def success[R](v: R): DbAction[R] = DBIOAction.successful(v)

}
