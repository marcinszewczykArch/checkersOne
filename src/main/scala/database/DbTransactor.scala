package database

import cats.effect._
import database.DbConfig._
import doobie.Transactor

import scala.concurrent.ExecutionContext

object DbTransactor {
  val dbExecutionContext                      = ExecutionContext.global
  implicit val contextShift: ContextShift[IO] = IO.contextShift(dbExecutionContext)

  def transactor =
    Transactor.fromDriverManager[IO](
      driver = dbDriverName,
      url = dbUrl,
      user = dbUser,
      pass = dbPwd
    )

}
