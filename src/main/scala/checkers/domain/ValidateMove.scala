package checkers.domain

import checkers.domain.MoveValidationError._
import checkers.domain.ValidateMove.ErrorOr
import checkers.domain.PawnMoveType._
import checkers.domain.PawnType.{Queen, Regular}
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
      _         <- moveIsDiagonal(move)
      moveType  <- getMoveType(gameState, move)
      _         <- moveTypeIsCorrect(moveType, isSthToSmash(gameState))
      gameState <- getNewState(gameState, move, moveType)
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

    def moveIsDiagonal(move: PawnMove): ErrorOr[PawnMove] = {
      Either.cond(
        test  = (move.to.x - move.from.x).abs == (move.to.y - move.from.y).abs,
        right = move,
        left  = MoveIsNotDiagonal
      )
    }

    def moveTypeIsCorrect(moveType: PawnMoveType, isWithSmash: Boolean): ErrorOr[PawnMoveType] = {
      Either.cond(
        test  = (isWithSmash && moveType == PawnMoveType.WithSmash) || (!isWithSmash && moveType == PawnMoveType.Single),
        right = moveType,
        left  = MoveTypeIsIncorrect
      )
    }

    def getMoveType (gameState: GameState, move: PawnMove): ErrorOr[PawnMoveType] = {
      val pawnType = gameState.board.pawnAt(move.from).map(_.pawnType)

      pawnType match {
        case Some(PawnType.Regular) => getMoveTypeRegular (gameState: GameState, move: PawnMove)
        case Some(PawnType.Queen)   => getMoveTypeQueen   (gameState: GameState, move: PawnMove)
        case _                      => Left(IllegalMove)
      }
    }

    def getNewState(gameState: GameState, move: PawnMove, moveType: PawnMoveType): ErrorOr[GameState] = {

      moveType match {
        case Single => Right(
          GameState(
            status     = GameStatus.Ongoing,
            movesNow   = gameState.movesNow.opposite,
            board      = getBoardAfterMove(gameState, move).promoteForQueen(),
            nextMoveBy = None
          )
        )

        case WithSmash =>
          val isNextToSmash  = checkNextToSmash (gameState, move)
          val boardAfterMove = getBoardAfterMove(gameState, move)

          Right(
            GameState(
              status     = checkNewStatus(boardAfterMove),
              movesNow   = if (isNextToSmash) gameState.movesNow              else gameState.movesNow.opposite,
              board      = if (isNextToSmash) boardAfterMove                  else boardAfterMove.promoteForQueen(),
              nextMoveBy = if (isNextToSmash) gameState.board.pawnAt(move.to) else None
            )
          )

        case _ => Left(IllegalMove)
      }
    }



    private def getMoveTypeRegular (gameState: GameState, move: PawnMove): ErrorOr[PawnMoveType] = {
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

    private def getMoveTypeQueen (gameState: GameState, move: PawnMove): ErrorOr[PawnMoveType] = {
      val otherSide = gameState.movesNow.opposite

      val deltaX = move.to.x - move.from.x
      val deltaY = move.to.y - move.from.y

      val pawnsOnTheWay: List[Pawn] = for {
        dx <- if (deltaX > 0) List.range(1, deltaX) else List.range(deltaX, 0)
        dy <- if (deltaY > 0) List.range(1, deltaY) else List.range(deltaY, 0)
        if dx.abs == dy.abs
        pawnPosition = PawnPosition(move.from.x + dx, move.from.y + dy)
        pawn = gameState.board.pawnAt(pawnPosition)
        if pawn.isDefined
      } yield pawn.get

      pawnsOnTheWay.size match {
        case 0                                         => Right(Single)
        case 1 if pawnsOnTheWay.head.side == otherSide => Right(WithSmash)
        case 1                                         => Left(MoveValidationError.SmashingOwnPawnIsNotOk)
        case _                                         => Left(MoveValidationError.TooManyPawnsOnTheWay)
      }
    }

    private def isSthToSmash (gameState: GameState): Boolean = {
      val colour: Side            = gameState.movesNow
      val board: Board            = gameState.board
      val nextMoveBy:Option[Pawn] = gameState.nextMoveBy

      val pawnsToAnalyze: Array[Pawn] = {
          if (nextMoveBy.isDefined)
            Array(nextMoveBy.get)
          else
            board.pawnsArray
        }

      val checkForRegular: Boolean = //forComprehension
        pawnsToAnalyze
          .filter(_.side == colour)
          .filter(_.pawnType == PawnType.Regular)
          .exists(o => board.pawnsArray.exists(p =>
            p.side != colour && (
              (p.position == o.position.upRight()   && board.positionIsAvailable(p.position.upRight()))   ||
                (p.position == o.position.upLeft()    && board.positionIsAvailable(p.position.upLeft()))    ||
                (p.position == o.position.downRight() && board.positionIsAvailable(p.position.downRight())) ||
                (p.position == o.position.downLeft()  && board.positionIsAvailable(p.position.downLeft())))
          ))

      val checkForQueen: Boolean = {
        for {
          queen  <- pawnsToAnalyze.filter(_.side == colour).filter(_.pawnType == PawnType.Queen)
          deltaX <- List.range(2, 7).concat(List.range(-7, 2))
          deltaY <- List.range(2, 7).concat(List.range(-7, 2))

          delta    = (deltaX, deltaY) if deltaX.abs == deltaY.abs
          moveFrom = queen.position
          moveTo   = PawnPosition(moveFrom.x + delta._1, moveFrom.y + delta._2) if moveTo.isOnTheBoard && board.positionIsAvailable(moveTo)
          move     = PawnMove(moveFrom, moveTo) if getMoveTypeQueen(gameState, move) == Right(WithSmash)
          } yield move
      }.length > 0

      checkForRegular || checkForQueen
    }

    private def getSmashedPawn(gameState: GameState, move: PawnMove): Option[Pawn] = {
      val board = gameState.board
      val movesNow = gameState.movesNow

      board.pawnAt(move.from).map(_.pawnType) match {
        case Some(Regular) =>
          if (move.to == move.from.doubleUpRight())
            board.pawnAt(move.from.upRight())
          else if (move.to == move.from.doubleDownRight())
            board.pawnAt(move.from.downRight())
          else if (move.to == move.from.doubleUpLeft())
            board.pawnAt(move.from.upLeft())
          else if (move.to == move.from.doubleDownLeft())
            board.pawnAt(move.from.downLeft())
          else
            None

        case Some(Queen) => { //todo duplicated code from ValidateMove.validateMoveTypeQueen, should be connected somehow
          val otherSide = movesNow.opposite

          //check if move is diagonal
          if ((move.to.x - move.from.x).abs != (move.to.y - move.from.y).abs)
            return None

          val deltaX = move.to.x - move.from.x
          val deltaY = move.to.y - move.from.y

          val pawnsOnTheWay: List[Pawn] = for {
            dx <- if (deltaX > 0) List.range(1, deltaX) else List.range(deltaX, 0)
            dy <- if (deltaY > 0) List.range(1, deltaY) else List.range(deltaY, 0)
            if dx.abs == dy.abs
            pawnPosition = PawnPosition(move.from.x + dx, move.from.y + dy)
            pawn = board.pawnAt(pawnPosition)
            if pawn.isDefined
          } yield pawn.get

          if (pawnsOnTheWay.size == 1 && pawnsOnTheWay.head.side == otherSide)
            Some(pawnsOnTheWay.head)
          else
            None
        }
        case None => None
      }
    }

    private def getBoardAfterMove(gameState: GameState, move: PawnMove): Board = {
      val oldPawn = gameState.board.pawnAt(move.from).orNull
      val newPawn = Pawn(oldPawn.side, oldPawn.pawnType, move.to)

      val smashedPawn: Option[Pawn] = getSmashedPawn(gameState, move)

      Board(
        gameState.board.pawnsArray
          .filterNot(_ == oldPawn)
          .filterNot(_ == smashedPawn.orNull)
          .appended(newPawn))
    }

    private def checkNextToSmash (gameState: GameState, move: PawnMove): Boolean = {

        val newBoard: Board = getBoardAfterMove(gameState, move)

        //todo: check if game is finish (newBoard has only one colour pawns)

        val newState: GameState = GameState(
          status = GameStatus.Ongoing,
          movesNow = gameState.movesNow,
          board = newBoard,
          nextMoveBy = newBoard.pawnAt(move.to)
        )

        isSthToSmash(newState)
    }

    private def checkNewStatus(boardAfterMove: Board): GameStatus = {

      if (!boardAfterMove.pawnsArray.exists(_.side == White))
        GameStatus.Win(Red)
      else if (!boardAfterMove.pawnsArray.exists(_.side == Red))
        GameStatus.Win(White)
      //    else if (15 moves with queen without smashing) todo: add this condition
      //      GameStatus.Draw
      else
        GameStatus.Ongoing
    }


  }
}
