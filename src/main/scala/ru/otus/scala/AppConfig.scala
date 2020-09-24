package ru.otus.scala

object AppConfig {
  case class Config(db: DbConfig, server: ServerConfig)

  case class DbConfig(url: String, user: String, password: String)

  case class ServerConfig(host: String, port: Int)

  object Config {
    val default: Config = Config(
      db = DbConfig(
        url = "jdbc:postgresql://localhost:5432/book_db",
        user = "postgres",
        password = "testpassword"
      ),
      server = ServerConfig("localhost", 8080)
    )
  }
}
