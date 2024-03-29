package singlePlayer

import cats.effect.IO
import checkers.CheckersCodecs.gameStateEncoder
import checkers.domain._
import io.circe.syntax.EncoderOps
import org.http4s.Response
import org.http4s.circe._
import org.http4s.dsl.io._

import scala.annotation.tailrec

object Ai {

  @tailrec
  def makeAiMoveEasy(state: GameState): IO[Response[IO]] =
    ValidateMove().apply(generateRandomMove(), state) match {
      case Right(newState) => Ok(newState.asJson)
      case _               => makeAiMoveEasy(state)
    }

  def makeAiMoveMedium(state: GameState): IO[Response[IO]] = {
    val movesWithValues: List[(PawnMove, Int)] = generateAllMovesCombinationsWithValue(state)

    val bestValue: Int = movesWithValues
      .sortBy(_._2)
      .reverse
      .head
      ._2

    import scala.util.Random
    val bestMove = Random
      .shuffle(
        movesWithValues
          .filter(_._2 == bestValue)
      )
      .head
      ._1 //shuffle list of moves to avoid repeating the same movement

    ValidateMove().apply(bestMove, state) match {
      case Right(newState) => Ok(newState.asJson)
      case _               => NotAcceptable("no move options found")
    }
  }

  private def generateAllMovesCombinationsWithValue(state: GameState): List[(PawnMove, Int)] =
    (for {
      fx       <- 0 to 8
      fy       <- 0 to 8
      moveFrom  = PawnPosition(fx, fy)
      if moveFrom.isDefined

      tx       <- 0 to 8
      ty       <- 0 to 8
      moveTo    = PawnPosition(tx, ty)
      if moveTo.isDefined

      move      = PawnMove(moveFrom.get, moveTo.get)
      newState  = ValidateMove().apply(move, state)
      if newState.isRight
      moveValue = getMoveValue(state, newState.right.get)
    } yield (move, moveValue)).toList

  private def getMoveValue(state: GameState, newState: GameState): Int = {
    var sum = 0

    if (ValidateMove.isSthToSmash(newState) && state.movesNow != newState.movesNow) //opponent smash after move
      sum = sum - 2
    if (ValidateMove.isSthToSmash(newState) && state.movesNow == newState.movesNow) //next to smash after move
      sum = sum + 2
    if (
      ValidateMove.isSthToSmash(GameState(state.status, state.movesNow.opposite, state.board)) &&
      !ValidateMove.isSthToSmash(newState) && state.movesNow != newState.movesNow
    )                                                                               //opponent has sth to smash and we want to block it
      sum = sum + 2
    if (
      ValidateMove.isSthToSmash(GameState(newState.status, state.movesNow, newState.board))
    )                                                                               //opponent pawn to smash after move, but opponent turn
      sum = sum + 1
    if (
      newState.board.pawns.values.count(o =>
        o.side == state.movesNow && o.pawnType == PawnType.Queen
      ) > state.board.pawns.values.count(o => o.side == state.movesNow && o.pawnType == PawnType.Queen)
    )                                                                               //queen after move
      sum = sum + 1
    if (ValidateMove.isBlocked(newState.board, state.movesNow))                     //looser after move
      sum = sum - 5
    if (ValidateMove.isBlocked(newState.board, state.movesNow.opposite))            //winner after move
      sum = sum + 5
    if (newState.status != GameStatus.Ongoing)                                      //winner after move
      sum = sum + 5

    sum
  }

  private def generateRandomMove(): PawnMove = {
    import scala.util.Random
    val boardSize        = PawnPosition.availablePositions.size
    val moveFrom: String = Random.between(0, boardSize).toString
    val moveTo: String   = Random.between(0, boardSize).toString

    PawnMove
      .fromString(moveFrom, moveTo)
      .get //.get can be used here as the number is always from defined range (from 0 to boardSize)
  }

}
