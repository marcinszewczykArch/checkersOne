package database

import cats.effect.IO
import checkers.CheckersCodecs._
import checkers.domain.GameState
import database.DbTransactor.transactor
import doobie.implicits._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DatabaseRoutes {

  implicit val decodeGameState: EntityDecoder[IO, GameState] = jsonOf[IO, GameState]
  val databaseRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root / "state"        =>
      val gameStates: List[GameStateTo] = sql"select timestamp, status, movesNow, board, nextMoveBy from game_state"
        .query[GameStateTo]
        .to[List]
        .transact(transactor)
        .unsafeRunSync()

      Ok(gameStates)

    case req @ POST -> Root / "state" =>
      implicit val decodeGameStateTo: EntityDecoder[IO, GameStateTo] = jsonOf[IO, GameStateTo]
      implicit val encodeGameStateTo: EntityEncoder[IO, GameStateTo] = jsonEncoderOf[IO, GameStateTo]

      req.as[GameStateTo].flatMap { gameState =>
        val timestamp  = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss:SSS").format(LocalDateTime.now)
        val status     = gameState.status
        val movesNow   = gameState.movesNow
        val board      = gameState.board
        val nextMoveBy = gameState.nextMoveBy

        sql"""
          INSERT INTO
              game_state (timestamp, status, movesNow, board, nextMoveBy)
          VALUES ($timestamp, $status, $movesNow, $board, $nextMoveBy)
          """.update.run.transact(transactor).unsafeRunSync()

        Ok(GameStateTo(timestamp, status, movesNow, board, nextMoveBy))
      }

  }
  implicit val gameStateToEncoder: Encoder[GameStateTo]                = deriveEncoder[GameStateTo]
  implicit val gameStateToDecoder: Decoder[GameStateTo]                = deriveDecoder[GameStateTo]
  implicit val decodeGameStateTo: EntityDecoder[IO, List[GameStateTo]] = jsonOf[IO, List[GameStateTo]]
  implicit val encodeGameStateTo: EntityEncoder[IO, List[GameStateTo]] = jsonEncoderOf[IO, List[GameStateTo]]

  case class GameStateTo(
    timestamp: String,
    status: String,
    movesNow: String,
    board: String,
    nextMoveBy: String
  )
  //todo: add:
  //playerWhite: String,
  //playerRed: String
  //room: String

}
