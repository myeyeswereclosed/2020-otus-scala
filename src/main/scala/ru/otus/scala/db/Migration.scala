package ru.otus.scala.db

import org.flywaydb.core.Flyway
import ru.otus.scala.config.AppConfig.DbConfig

class Migration(config: DbConfig) {
  def run(): Unit = {
    println(s"Starting migration ($config)")

    Flyway
      .configure()
      .dataSource(config.url, config.user, config.password)
      .load()
      .migrate()

    println(s"Migration finished")
  }
}
