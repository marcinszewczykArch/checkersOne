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
      _         <- destinationPositionIsAvailable(gameState, move)
      _         <- pawnColourIsCorrect(pawn, gameState, move)
      _         <- pawnIsCorrectIfMultipleSmashingContinues(gameState, move)
      moveType  <- validateMoveType(gameState, move)
      gameState <- getNewState(gameState, moveType, move)
    } yield gameState

    def startAndDestinationCoordinatesDiffer(move: PawnMove): ErrorOr[PawnMove] =
      Either.cond(
        test  = move.from != move.to,
        right = move,
        left  = IdenticalStartAndDestinationPosition
      )

    def destinationPositionIsAvailable(gameState: GameState, move: PawnMove): ErrorOr[PawnMove] =
      Either.cond(
        test  = gameState.board.positionIsAvailable(move.to) == true,
        right = move,
        left  = DestinationNotAvailable
      )

    def pawnColourIsCorrect(pawn: Pawn, gameState: GameState, move: PawnMove): ErrorOr[PawnMove] =
      Either.cond(
        test  = pawn.side == gameState.movesNow,
        right = move,
        left  = WrongPawnColor
      )

    def pawnIsCorrectIfMultipleSmashingContinues(gameState: GameState, move: PawnMove): ErrorOr[PawnMove] =
      Either.cond(
        test  = gameState.nextMoveBy.isEmpty || gameState.nextMoveBy == gameState.board.pawnAt(move.from),
        right = move,
        left  = ContinueMultipleSmashing
      )

    def validateMoveType (gameState: GameState, move: PawnMove): ErrorOr[PawnMoveType] = {
      val pawnType = gameState.board.pawnAt(move.from).map(_.pawnType)

      pawnType match {
        case Some(PawnType.Regular) => validateMoveTypeRegular(gameState: GameState, move: PawnMove)
        case Some(PawnType.Queen)   => validateMoveTypeQueen(gameState: GameState, move: PawnMove)
        case _                      => Left(MoveValidationError.IllegalMove)
      }
    }

    def validateMoveTypeRegular (gameState: GameState, move: PawnMove): ErrorOr[PawnMoveType] = {
      val otherSide = gameState.movesNow.opposite
      val thisSide = gameState.movesNow
      val board = gameState.board

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

    //todo: implement validation for queen
    def validateMoveTypeQueen (gameState: GameState, move: PawnMove): ErrorOr[PawnMoveType] = {
      val otherSide = gameState.movesNow.opposite
      val thisSide = gameState.movesNow
      val board = gameState.board

      //check if move is diagonal
      if ((move.to.x - move.from.x).abs != (move.to.y - move.from.y).abs)
        Left(MoveValidationError.MoveIsNotDiagonal)
      else
        ???

      val deltaX = move.to.x - move.from.x
      val deltaY = move.to.y - move.from.y

      val pawnsOnTheWay: List[Pawn] = for {
        x <- List.range(1, deltaX)
        y <- List.range(1, deltaY)
        if x.abs == y.abs
        pawnPosition = PawnPosition(move.from.x + x, move.from.y + y)
        pawn = board.pawnAt(pawnPosition)
        if pawn.isDefined
      } yield pawn.get

//      (1 to moveLength)
//        .map(o => PawnPosition(move.from.x + o, move.from.y + o))
//        .filter(board.pawnAt(_).isDefined)


      //check if there is
      // 1) 0 other pawns on the way => Right(Single)
      // 2) exactly 1 pawn of opponent on the way => Right(WithSmash)
      // 3) other option => Left

    }



    def getNewState(gameState: GameState, moveType: PawnMoveType, move: PawnMove): ErrorOr[GameState] = {
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
