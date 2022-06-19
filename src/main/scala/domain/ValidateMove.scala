package domain

import domain.MoveValidationError._
import domain.ValidateMove.ErrorOr
import domain.PawnMoveType._
import domain.Side._

trait ValidateMove {
  def apply(
             move: PawnMove,
             gameState: GameState
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
      val fx = move.from.x
      val fy = move.from.y
      val tx = move.to.x
      val ty = move.to.y
      val otherSide = gameState.movesNow.opposite
      val thisSide  = gameState.movesNow
      val board  = gameState.board

      if      ((tx, ty) == (fx - 1, fy + 1) && thisSide == White)
        Right(Single)
      else if ((tx, ty) == (fx - 1, fy - 1) && thisSide == White)
        Right(Single)
      else if ((tx, ty) == (fx + 1, fy + 1) && thisSide == Red)
        Right(Single)
      else if ((tx, ty) == (fx + 1, fy - 1) && thisSide == Red)
        Right(Single)

      else if ((tx, ty) == (fx + 2, fy + 2) && board.pawnExists(PawnPosition(fx + 1, fy + 1), otherSide))
        Right(WithSmash)
      else if ((tx, ty) == (fx - 2, fy - 2) && board.pawnExists(PawnPosition(fx - 1, fy - 1), otherSide))
        Right(WithSmash)
      else if ((tx, ty) == (fx + 2, fy - 2) && board.pawnExists(PawnPosition(fx + 1, fy - 1), otherSide))
        Right(WithSmash)
      else if ((tx, ty) == (fx - 2, fy + 2) && board.pawnExists(PawnPosition(fx - 1, fy + 1), otherSide))
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
