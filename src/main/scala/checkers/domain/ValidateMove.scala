package checkers.domain

import checkers.domain.MoveValidationError._
import checkers.domain.PawnMoveType._
import checkers.domain.PawnType.{Queen, Regular}
import checkers.domain.Side._
import checkers.domain.ValidateMove.ErrorOr

trait ValidateMove {
  def apply(
    move: PawnMove,
    gameState: GameState
  ): ErrorOr[GameState]
}

object ValidateMove {
  type ErrorOr[A] = Either[MoveValidationError, A]

  def apply(): ValidateMove =
    new ValidateMove {

      override def apply(move: PawnMove, gameState: GameState): ErrorOr[GameState] =
        for {
          _         <- gameIsOngoing(gameState)
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

      def gameIsOngoing(gameState: GameState): ErrorOr[GameState] =
        Either.cond(
          test = gameState.status == GameStatus.Ongoing,
          right = gameState,
          left = GameIsOver
        )

      def startAndDestinationCoordinatesDiffer(move: PawnMove): ErrorOr[PawnMove] =
        Either.cond(
          test = move.from != move.to,
          right = move,
          left = IdenticalStartAndDestinationPosition
        )

      def destinationPositionIsAvailable(gameState: GameState, move: PawnMove): ErrorOr[PawnMove] =
        Either.cond(
          test = gameState.board.positionIsAvailable(move.to) == true,
          right = move,
          left = DestinationNotAvailable
        )

      def pawnColourIsCorrect(
        pawn: Pawn,
        gameState: GameState,
        move: PawnMove
      ): ErrorOr[PawnMove] =
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

      def moveIsDiagonal(move: PawnMove): ErrorOr[PawnMove] =
        Either.cond(
          test = (move.to.x - move.from.x).abs == (move.to.y - move.from.y).abs,
          right = move,
          left = MoveIsNotDiagonal
        )

      def moveTypeIsCorrect(moveType: PawnMoveType, isWithSmash: Boolean): ErrorOr[PawnMoveType] =
        Either.cond(
          test =
            (isWithSmash && moveType == PawnMoveType.WithSmash) || (!isWithSmash && moveType == PawnMoveType.Single),
          right = moveType,
          left = MoveTypeIsIncorrect
        )

      def getMoveType(gameState: GameState, move: PawnMove): ErrorOr[PawnMoveType] = {
        val pawnType = gameState.board.pawnAt(move.from).map(_.pawnType)

        pawnType match {
          case Some(PawnType.Regular) => getMoveTypeRegular(gameState: GameState, move: PawnMove)
          case Some(PawnType.Queen)   => getMoveTypeQueen(gameState: GameState, move: PawnMove)
          case _                      => Left(IllegalMove)
        }
      }

      def getNewState(
        gameState: GameState,
        move: PawnMove,
        moveType: PawnMoveType
      ): ErrorOr[GameState] =
        moveType match {
          case Single    =>
            val boardAfterMove = getBoardAfterMove(gameState, move)

            Right(
              GameState(
                status = checkNewStatus(boardAfterMove, gameState.movesNow, Single),
                movesNow = gameState.movesNow.opposite,
                board = getBoardAfterMove(gameState, move).promoteForQueen(),
                nextMoveBy = None
              )
            )

          case WithSmash =>
            val isNextToSmash  = checkNextToSmash(gameState, move)
            val nextMoveType   = if (isNextToSmash) WithSmash else Single
            val boardAfterMove = getBoardAfterMove(gameState, move)

            Right(
              GameState(
                status = checkNewStatus(boardAfterMove, gameState.movesNow, nextMoveType),
                movesNow = if (isNextToSmash) gameState.movesNow else gameState.movesNow.opposite,
                board = if (isNextToSmash) boardAfterMove else boardAfterMove.promoteForQueen(),
                nextMoveBy = if (isNextToSmash) boardAfterMove.pawnAt(move.to) else None
              )
            )

          case _         => Left(IllegalMove)
        }

      private def getMoveTypeRegular(gameState: GameState, move: PawnMove): ErrorOr[PawnMoveType] = {
        val movesNow = gameState.movesNow
        val opponent = movesNow.opposite

        move.from.x - move.to.x match {
          case 1 if movesNow == White                                             => Right(Single)
          case -1 if movesNow == Red                                              => Right(Single)
          case 2 if getPawnsOnTheWay(gameState, move).exists(_.side == opponent)  => Right(WithSmash)
          case -2 if getPawnsOnTheWay(gameState, move).exists(_.side == opponent) => Right(WithSmash)
          case _                                                                  => Left(IllegalMove)
        }
      }

      private def getMoveTypeQueen(gameState: GameState, move: PawnMove): ErrorOr[PawnMoveType] = {
        val pawnsOnTheWay: List[Pawn] = getPawnsOnTheWay(gameState, move)

        pawnsOnTheWay.size match {
          case 0                                                           => Right(Single)
          case 1 if pawnsOnTheWay.head.side == gameState.movesNow.opposite => Right(WithSmash)
          case 1                                                           => Left(SmashingOwnPawnIsNotOk)
          case _                                                           => Left(TooManyPawnsOnTheWay)
        }
      }

      private def isSthToSmash(gameState: GameState): Boolean = {

        val pawnsToAnalyze: List[Pawn] = gameState.nextMoveBy
          .map(List(_))
          .getOrElse(gameState.board.pawnsList.filter(_.side == gameState.movesNow))

        val movesWithSmashForRegular = for {
          pawn   <- pawnsToAnalyze.filter(_.pawnType == PawnType.Regular)
          deltaX <- List(-2, 2)
          deltaY <- List(-2, 2)

          delta    = (deltaX, deltaY)
          moveFrom = pawn.position
          moveTo   = PawnPosition(moveFrom.x + delta._1, moveFrom.y + delta._2)
          if moveTo.isOnTheBoard && gameState.board.positionIsAvailable(moveTo)
          move     = PawnMove(moveFrom, moveTo) if getMoveTypeRegular(gameState, move) == Right(WithSmash)
        } yield move

        val movesWithSmashForQueen = for {
          queen  <- pawnsToAnalyze.filter(_.pawnType == PawnType.Queen)
          deltaX <- List.range(2, 7).concat(List.range(-7, 2))
          deltaY <- List.range(2, 7).concat(List.range(-7, 2))

          delta    = (deltaX, deltaY) if deltaX.abs == deltaY.abs
          moveFrom = queen.position
          moveTo   = PawnPosition(moveFrom.x + delta._1, moveFrom.y + delta._2)
          if moveTo.isOnTheBoard && gameState.board.positionIsAvailable(moveTo)
          move     = PawnMove(moveFrom, moveTo) if getMoveTypeQueen(gameState, move) == Right(WithSmash)
        } yield move

        (movesWithSmashForRegular ++ movesWithSmashForQueen).nonEmpty
      }

      private def getSmashedPawn(gameState: GameState, move: PawnMove): Option[Pawn] =
        gameState.board.pawnAt(move.from).map(_.pawnType) match {

          case Some(Regular) =>
            val x = Array(move.from.x, move.to.x).min + 1
            val y = Array(move.from.y, move.to.y).min + 1
            gameState.board.pawnAt(PawnPosition(x, y))

          case Some(Queen)   =>
            val pawnsOnTheWay: List[Pawn] = getPawnsOnTheWay(gameState, move)
            pawnsOnTheWay match {
              case List(pawn) => Some(pawn)
              case _          => None
            }

          case _             => None
        }

      private def getPawnsOnTheWay(gameState: GameState, move: PawnMove): List[Pawn] = {
        val deltaX = move.to.x - move.from.x
        val deltaY = move.to.y - move.from.y

        for {
          dx          <- if (deltaX > 0) List.range(1, deltaX) else List.range(deltaX, 0)
          dy          <- if (deltaY > 0) List.range(1, deltaY) else List.range(deltaY, 0)
          if dx.abs == dy.abs
          pawnPosition = PawnPosition(move.from.x + dx, move.from.y + dy)
          pawn         = gameState.board.pawnAt(pawnPosition)
          if pawn.isDefined
        } yield pawn.get
      }

      private def getBoardAfterMove(gameState: GameState, move: PawnMove): Board = {
        val oldPawn = gameState.board.pawnAt(move.from).orNull
        val newPawn = Pawn(oldPawn.side, oldPawn.pawnType, move.to)

        val smashedPawn: Option[Pawn] = getSmashedPawn(gameState, move)

        Board(
          gameState.board.pawnsList
            .filterNot(_ == oldPawn)
            .filterNot(_ == smashedPawn.orNull)
            .appended(newPawn)
        )
      }

      private def checkNextToSmash(gameState: GameState, move: PawnMove): Boolean = {

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

      //todo: add tests
      private def isBlocked(board: Board, side: Side): Boolean = {
        val tempState = GameState(status = GameStatus.Ongoing, movesNow = side, board = board, nextMoveBy = None)

        if (isSthToSmash(tempState))
          false //player has sth to smash, so it is not blocked
        else { //nothing to smash - check if it is possible to move
          val availablePositions =
            for {
              pawn    <- board.pawnsList.filter(_.side == side)
              delta   <- List(-1, 1)
              position = if (pawn.pawnType == Queen)
                           PawnPosition(pawn.position.x + delta, pawn.position.y + delta)
                         else if (pawn.side == White)
                           PawnPosition(pawn.position.x - 1, pawn.position.y + delta)
                         else
                           PawnPosition(pawn.position.x + 1, pawn.position.y + delta)
              if board.positionIsAvailable(position) == true
            } yield position

          availablePositions.isEmpty //no available positions to move, player is blocked
        }
      }

      private def checkNewStatus(
        boardAfterMove: Board,
        movesNow: Side,
        nextMoveType: PawnMoveType
      ): GameStatus =
        nextMoveType match {
          case Single    =>
            if (isBlocked(boardAfterMove, movesNow.opposite))
              GameStatus.setWinner(movesNow)
            else
              GameStatus.Ongoing

          case WithSmash =>
            if (!boardAfterMove.pawnsList.exists(_.side == movesNow.opposite))
              GameStatus.setWinner(movesNow)
            else
              GameStatus.Ongoing
        }
    }
}
