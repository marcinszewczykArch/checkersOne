package singlePlayer

import cats.effect.IO
import checkers.CheckersCodecs.gameStateEncoder
import checkers.domain.{GameState, PawnMove, PawnPosition, ValidateMove}
import io.circe.syntax.EncoderOps
import org.http4s.Response
import org.http4s.circe._
import org.http4s.dsl.io._
import singlePlayer.AiMedium.{evaluateMove, generateMove}

import scala.annotation.tailrec
object AiEasy {

  @tailrec
  def makeAiMoveEasy(state: GameState): IO[Response[IO]] = {
    import scala.util.Random
    val boardSize        = PawnPosition.availablePositions.size
    val moveFrom: String = Random.between(0, boardSize).toString
    val moveTo: String   = Random.between(0, boardSize).toString

    val move: PawnMove =
      PawnMove
        .fromString(moveFrom, moveTo)
        .get //.get can be used here as the number is always from defined range (from 0 to boardSize)

    ValidateMove().apply(move, state) match {
      case Right(newState) =>
        println(moveFrom + " -> " + moveTo)
        Ok(newState.asJson)
      case Left(_)         => makeAiMoveEasy(state)
    }
  }

  def makeAiMoveMedium(state: GameState): IO[Response[IO]] = {
    val allMoves = (0 to 10_000)
      .map(_ => generateMove())
      .map(move => evaluateMove(move, state))
      .filter(_.isDefined)
      .sortBy(_.get._2)
      .reverse

    val bestMove = allMoves
      .head
      .map(o => ValidateMove().apply(o._1, state))
      .get

    bestMove match {
      case Right(newState) => Ok(newState.asJson)
      case Left(_)         => NotAcceptable("no move options found")
    }



  }

}
