package database

import cats.implicits.catsSyntaxApply
import checkers.domain.{Board, GameStatus, Side}
import database.DatabaseTransactor.transactor
import doobie.implicits._
import cats.syntax.show._
import checkers.domain.Board.showBoard

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Doobie {

  val initialSchema = {

    //drop table
    val dropGameStateTable =
      sql"""
    DROP TABLE IF EXISTS game_state
  """

    //create table
    val createGameStateTable =
      sql"""
         CREATE table game_state(
             timestamp VARCHAR(23),
             status VARCHAR(23), 
             movesNow VARCHAR(1), 
             board VARCHAR(32),
             nextMoveFrom VARCHAR(2),
             saveName VARCHAR(80)                  
             )
             """

    //insert initial state
    val timestamp    = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss:SSS").format(LocalDateTime.now)
    val status       = GameStatus.Ongoing.tag
    val movesNow     = Side.White.tag
    val board        = Board.initial.show
    val nextMoveFrom = ""
    val saveName     = "initial state"

    val insertInitialState =
      sql"""
        INSERT INTO
            game_state (timestamp, status, movesNow, board, nextMoveFrom, saveName)
            VALUES ($timestamp, $status, $movesNow, $board, $nextMoveFrom, $saveName)
            """

    //insert initial state all queen
    val boardAllQueen              = "RRRRRRRRRRRRooooooooWWWWWWWWWWWW"
    val saveNameAllQueen           = "initial state all Queen"
    val insertInitialStateAllQueen =
      sql"""
        INSERT INTO
            game_state (timestamp, status, movesNow, board, nextMoveFrom, saveName)
            VALUES ($timestamp, $status, $movesNow, $boardAllQueen, $nextMoveFrom, $saveNameAllQueen)
            """

    //insert state obligatory smashing
    val boardWithMultipleSmash       = "roorooooorroowoooooooooowooooooo"
    val saveWithMultipleSmash        = "state with multiple smash"
    val insertStateWithMultipleSmash =
      sql"""
        INSERT INTO
            game_state (timestamp, status, movesNow, board, nextMoveFrom, saveName)
            VALUES ($timestamp, $status, $movesNow, $boardWithMultipleSmash, $nextMoveFrom, $saveWithMultipleSmash)
            """

    //insert state getting queen
    val boardWithGettingQueen       = "roororoowoooowoooooooooowooooooo"
    val saveWithGettingQueen        = "state with getting queen"
    val insertStateWithGettingQueen =
      sql"""
        INSERT INTO
            game_state (timestamp, status, movesNow, board, nextMoveFrom, saveName)
            VALUES ($timestamp, $status, $movesNow, $boardWithGettingQueen, $nextMoveFrom, $saveWithGettingQueen)
            """

    //insert state multiple smashing not getting queen
    val boardWithNotGettingQueen       = "roororrowoooowoooooooooowooooooo"
    val saveWithNotGettingQueen        = "state with multiple smashing not getting queen"
    val insertStateWithNotGettingQueen =
      sql"""
        INSERT INTO
            game_state (timestamp, status, movesNow, board, nextMoveFrom, saveName)
            VALUES ($timestamp, $status, $movesNow, $boardWithNotGettingQueen, $nextMoveFrom, $saveWithNotGettingQueen)
            """

    //insert state multiple smashing and another single smash
    val boardWithMultipleSmashingAndAnother       = "roororrowoooowoooooooroowooooooo"
    val saveWithMultipleSmashingAndAnother        = "state with multiple smashing and another single smash"
    val insertStateWithMultipleSmashingAndAnother =
      sql"""
        INSERT INTO
            game_state (timestamp, status, movesNow, board, nextMoveFrom, saveName)
            VALUES ($timestamp, $status, $movesNow, $boardWithMultipleSmashingAndAnother, $nextMoveFrom, $saveWithMultipleSmashingAndAnother)
            """

    //insert state multiple smashing with queen
    val boardWithMultipleSmashingWithQueen       = "oooooooooroooorrWooooorroooooooo"
    val saveWithMultipleSmashingWithQueen        = "state with multiple smashing with queen"
    val insertStateWithMultipleSmashingWithQueen =
      sql"""
        INSERT INTO
            game_state (timestamp, status, movesNow, board, nextMoveFrom, saveName)
            VALUES ($timestamp, $status, $movesNow, $boardWithMultipleSmashingWithQueen, $nextMoveFrom, $saveWithMultipleSmashingWithQueen)
            """

    //insert state with blocked player
    val boardWithBlockedPlayer       = "ooooooorWorroowroowwoowooooooooo"
    val saveWithBlockedPlayer        = "state with blocked player"
    val insertStateWithBlockedPlayer =
      sql"""
        INSERT INTO
            game_state (timestamp, status, movesNow, board, nextMoveFrom, saveName)
            VALUES ($timestamp, $status, $movesNow, $boardWithBlockedPlayer, $nextMoveFrom, $saveWithBlockedPlayer)
            """

    dropGameStateTable.update.run *>
      createGameStateTable.update.run *>
      insertInitialState.update.run *>
      insertInitialStateAllQueen.update.run *>
      insertStateWithGettingQueen.update.run *>
      insertStateWithNotGettingQueen.update.run *>
      insertStateWithMultipleSmash.update.run *>
      insertStateWithMultipleSmashingAndAnother.update.run *>
      insertStateWithBlockedPlayer.update.run *>
      insertStateWithMultipleSmashingWithQueen.update.run

  }.transact(transactor)

}
