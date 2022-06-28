package checkers.domain

import checkers.domain.MoveValidationError._
import checkers.domain.ValidateMove.ErrorOr
import checkers.domain.PawnMoveType._
import checkers.domain.Side._

trait ValidateMove {
  def apply(
             move: PawnMove,
             gameState: GameState //todo: this take game state, but from user we get only board and current colour (others are by default)
           ): ErrorOr[GameState]
}

object ValidateMove {
  type ErrorOr[A] = Either[MoveValidationError, A]

  def apply(): ValidateMove = new ValidateMove {

    override def apply(move: PawnMove, gameState: GameState): ErrorOr[GameState] = for {
      pawn      <- gameState.board.pawnAt(move.from).toRight(NoPawnAtStartingPosition)
      _         <- startAndDestinationCoordinatesDiffer(move)
      _         <- pawnColourIsCorrect(pawn, gameState, move)
      _         <- pawnIsCorrectIfMultipleSmashingContinues(gameState, move)
      moveType  <- validateMoveType(gameState, move)
      gameState <- smashIfNecessary(gameState, moveType, move)
    } yield gameState

    def startAndDestinationCoordinatesDiffer(move: PawnMove): ErrorOr[PawnMove] =
      Either.cond(
        test = move.from != move.to,
        right = move,
        left = IdenticalStartAndDestinationPosition
      )

    def pawnColourIsCorrect(pawn: Pawn, gameState: GameState, move: PawnMove): ErrorOr[PawnMove] =
      Either.cond(
        test = pawn.side == gameState.movesNow,
        right = move,
        left = WrongPawnColor
      )

    def pawnIsCorrectIfMultipleSmashingContinues(gameState: GameState, move: PawnMove): ErrorOr[PawnMove] =
      Either.cond(
        test = gameState.nextMoveBy.isEmpty || gameState.nextMoveBy == gameState.board.pawnAt(move.from),
        right = move,
        left = ContinueMultipleSmashing
      )

    def validateMoveType(gameState: GameState, move: PawnMove): ErrorOr[PawnMoveType] = {
      val otherSide = gameState.movesNow.opposite
      val thisSide  = gameState.movesNow
      val board  = gameState.board

      if      (move.to == move.from.upLeft()    && thisSide == White)
        Right(Single)
      else if (move.to == move.from.downLeft()  && thisSide == White)
        Right(Single)
      else if (move.to == move.from.upRight()   && thisSide == Red)
        Right(Single)
      else if (move.to == move.from.downRight() && thisSide == Red)
        Right(Single)

      else if (move.to == move.from.doubleUpLeft()    && board.pawnExists(move.from.upLeft(), otherSide))
        Right(WithSmash)
      else if (move.to == move.from.doubleDownLeft()  && board.pawnExists(move.from.downLeft(), otherSide))
        Right(WithSmash)
      else if (move.to == move.from.doubleUpRight()   && board.pawnExists(move.from.upRight(), otherSide))
        Right(WithSmash)
      else if (move.to == move.from.doubleDownRight() && board.pawnExists(move.from.downRight(), otherSide))
        Right(WithSmash)

      else
        Left(MoveValidationError.IllegalMove)
    }

    def smashIfNecessary(gameState: GameState, moveType: PawnMoveType, move: PawnMove): ErrorOr[GameState] = {
      val sthToSmash: Boolean = gameState.isSthToSmash

      moveType match {
        case Single     if !sthToSmash  => Right(gameState.getNewState(move))
        case WithSmash  if sthToSmash   => Right(gameState.getNewState(move))
        case Single     if sthToSmash   => Left(OpponentPawnToTake)
        case _                          => Left(IllegalMove)
      }
    }

  }
}