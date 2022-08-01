package database

import cats.implicits.catsSyntaxApply
import checkers.domain.{Board, GameStatus, Side}
import database.DbTransactor.transactor
import doobie.implicits._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Doobie {

//  val operation1: ConnectionIO[String] = "42".pure[ConnectionIO]
//  val result1: IO[String]              = operation1.transact(transactor)
//
//  val operation2: ConnectionIO[List[String]] = sql"select name from city limit 5".query[String].to[List]
//  val result2: IO[List[String]]              = operation2.transact(transactor)
//
//  val operation3             = sql"SELECT name FROM person".query[Int].to[List]
//  val result3: IO[List[Int]] = operation3.transact(transactor)

  val initialSchema = {
    val dropGameStateTable =
      sql"""
    DROP TABLE IF EXISTS game_state
  """

    val createGameStateTable =
      sql"""
         CREATE table game_state(
             timestamp VARCHAR(23),
             status VARCHAR(23), 
             movesNow VARCHAR(1), 
             board VARCHAR(32),
             nextMoveBy VARCHAR(2),
             saveName VARCHAR(80)                  
             )
             """

    val timestamp  = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss:SSS").format(LocalDateTime.now)
    val status     = GameStatus.Ongoing.tag
    val movesNow   = Side.White.tag
    val board      = Board.initial.toString
    val nextMoveBy = ""
    val saveName   = "initial state"

    val insertInitialState =
      sql"""
        INSERT INTO
            game_state (timestamp, status, movesNow, board, nextMoveBy, saveName)
            VALUES ($timestamp, $status, $movesNow, $board, $nextMoveBy, $saveName)
            """

    val boardAllQueen              = "RRRRRRRRRRRRooooooooWWWWWWWWWWWW"
    val saveNameAllQueen           = "initial state all Queen"
    val insertInitialStateAllQueen =
      sql"""
        INSERT INTO
            game_state (timestamp, status, movesNow, board, nextMoveBy, saveName)
            VALUES ($timestamp, $status, $movesNow, $boardAllQueen, $nextMoveBy, $saveNameAllQueen)
            """

    dropGameStateTable.update.run *>
      createGameStateTable.update.run *>
      insertInitialState.update.run *>
      insertInitialStateAllQueen.update.run
  }.transact(transactor)

}
