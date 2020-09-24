package ru.otus.scala.db

import org.flywaydb.core.Flyway
import ru.otus.scala.AppConfig.DbConfig

class Migration(config: DbConfig) {
  def run(): Unit = {
    println("Starting " + config)

    Flyway
      .configure()
      .dataSource(config.url, config.user, config.password)
      .load()
      .migrate()

    println("Finished")
  }
}
