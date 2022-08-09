package singlePlayer

import cats.effect.IO
import checkers.domain._
import io.circe.syntax.EncoderOps
import org.http4s.Response
import org.http4s.circe._
import org.http4s.dsl.io.{Ok, _}
import checkers.CheckersCodecs.gameStateEncoder
import checkers.domain.ValidateMove.ErrorOr
import singlePlayer.AiEasy._
import cats.effect.IO
import checkers.CheckersCodecs.gameStateEncoder
import checkers.domain.{GameState, PawnMove, PawnPosition, ValidateMove}
import io.circe.syntax.EncoderOps
import org.http4s.Response
import org.http4s.circe._
import org.http4s.dsl.io._

object AiMedium {

  def generateMove(): PawnMove = {
    import scala.util.Random
    val boardSize = PawnPosition.availablePositions.size
    val moveFrom: String = Random.between(0, boardSize).toString
    val moveTo: String = Random.between(0, boardSize).toString

    PawnMove
      .fromString(moveFrom, moveTo)
      .get //.get can be used here as the number is always from defined range (from 0 to boardSize)
  }

  def evaluateMove(move: PawnMove, state: GameState): Option[(PawnMove, Int)] =
    ValidateMove().apply(move, state) match {
      case Right(newState) => Some(move, getMoveValue(state, newState))
      case Left(_) => None
    }


  def getMoveValue(state: GameState, newState: GameState): Int = {
    if (ValidateMove.isSthToSmash(newState) && state.movesNow != newState.movesNow) //opponent smash after move
      -1
    else if (ValidateMove.isSthToSmash(newState) && state.movesNow == newState.movesNow) //next to smash after move
      1
    else if (ValidateMove.isSthToSmash(GameState(state.status, state.movesNow.opposite, state.board)) && !ValidateMove.isSthToSmash(newState) && state.movesNow != newState.movesNow) //opponent has sth to smash and we want to block it
      1
    else if (ValidateMove.isSthToSmash(GameState(newState.status, state.movesNow, newState.board))) //opponent pawn to smash after move, but opponent turn
      1
    else if (newState.board.pawns.values.count(o => o.side == state.movesNow && o.pawnType == PawnType.Queen) > state.board.pawns.values.count(o => o.side == state.movesNow && o.pawnType == PawnType.Queen) ) //queen after move
      1
    else if (ValidateMove.isBlocked(newState.board, state.movesNow)) //looser after move
      -5
    else if (ValidateMove.isBlocked(newState.board, state.movesNow.opposite)) //winner after move
      5
    else if (newState.status != GameStatus.Ongoing) //winner after move
      5
    else
      0
  }



}








