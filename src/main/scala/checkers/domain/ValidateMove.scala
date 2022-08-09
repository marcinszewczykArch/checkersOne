package checkers.domain

import checkers.domain.MoveValidationError._
import checkers.domain.PawnMoveType._
import checkers.domain.PawnType.{Queen, Regular}
import checkers.domain.Side._
import checkers.domain.ValidateMove.ErrorOr

import scala.collection.Map

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
          test = gameState.board.positionIsAvailable(move.to),
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
          test = gameState.nextMoveFrom.isEmpty || gameState.nextMoveFrom.get == move.from,
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
          test = (isWithSmash && moveType == WithSmash) || (!isWithSmash && moveType == Single),
          right = moveType,
          left = MoveTypeIsIncorrect
        )

    }

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
            nextMoveFrom = None
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
            nextMoveFrom = if (isNextToSmash) Some(move.to) else None
          )
        )

      case _         => Left(IllegalMove)
    }

  def getMoveTypeRegular(gameState: GameState, move: PawnMove): ErrorOr[PawnMoveType] = {
    val movesNow = gameState.movesNow
    val opponent = movesNow.opposite

    move.from.x - move.to.x match {
      case 1 if movesNow == White => Right(Single)
      case 1 if movesNow == Red   => Left(BackwardMoveNotAllowed)

      case -1 if movesNow == Red   => Right(Single)
      case -1 if movesNow == White => Left(BackwardMoveNotAllowed)

      case 2 | -2 if getPawnsOnTheWay(gameState, move).exists(_._2.side == opponent) => Right(WithSmash)

      case _ => Left(TooLongMoveForRegular)
    }
  }

  def getMoveTypeQueen(gameState: GameState, move: PawnMove): ErrorOr[PawnMoveType] = {
    val pawnsOnTheWay: Map[PawnPosition, Pawn] = getPawnsOnTheWay(gameState, move)

    pawnsOnTheWay.size match {
      case 0                                                              => Right(Single)
      case 1 if pawnsOnTheWay.head._2.side == gameState.movesNow.opposite => Right(WithSmash)
      case 1                                                              => Left(SmashingOwnPawnIsNotOk)
      case _                                                              => Left(TooManyPawnsOnTheWay)
    }
  }

  def isSthToSmash(gameState: GameState): Boolean = {

    val pawnsToAnalyze: Map[PawnPosition, Pawn] = gameState.nextMoveFrom
      .map(o => Map(o -> gameState.board.pawnAt(o).get))
      .getOrElse(gameState.board.pawns.filter(_._2.side == gameState.movesNow))

    val movesWithSmashForRegular = for {
      pawn   <- pawnsToAnalyze.filter(_._2.pawnType == PawnType.Regular)
      deltaX <- List(-2, 2)
      deltaY <- List(-2, 2)

      delta    = (deltaX, deltaY)
      moveFrom = pawn._1
      moveTo  <- PawnPosition.apply(moveFrom.x + delta._1, moveFrom.y + delta._2)
      if gameState.board.positionIsAvailable(moveTo)
      move     = PawnMove(moveFrom, moveTo) if getMoveTypeRegular(gameState, move) == Right(WithSmash)
    } yield move

    val movesWithSmashForQueen = for {
      queen  <- pawnsToAnalyze.filter(_._2.pawnType == PawnType.Queen)
      deltaX <- List.range(2, 8).concat(List.range(-7, 3))
      deltaY <- List.range(2, 8).concat(List.range(-7, 3))

      delta    = (deltaX, deltaY) if deltaX.abs == deltaY.abs
      moveFrom = queen._1
      moveTo  <- PawnPosition(moveFrom.x + delta._1, moveFrom.y + delta._2)
      if gameState.board.positionIsAvailable(moveTo)
      move     = PawnMove(moveFrom, moveTo) if getMoveTypeQueen(gameState, move) == Right(WithSmash)
    } yield move

    (movesWithSmashForRegular ++ movesWithSmashForQueen).nonEmpty
  }

  def getSmashedPawn(gameState: GameState, move: PawnMove): Option[(PawnPosition, Pawn)] =
    gameState.board.pawnAt(move.from).map(_.pawnType) match {

      case Some(Regular) =>
        val x = Array(move.from.x, move.to.x).min + 1
        val y = Array(move.from.y, move.to.y).min + 1
        for {
          position <- PawnPosition(x, y)
          pawn     <- gameState.board.pawnAt(position)
        } yield (position, pawn)

      case Some(Queen)   =>
        val pawnsOnTheWay: Map[PawnPosition, Pawn] = getPawnsOnTheWay(gameState, move)
        pawnsOnTheWay.size match {
          case 1 => Some(pawnsOnTheWay.head._1, pawnsOnTheWay.head._2)
          case _ => None
        }

      case _             => None
    }

  def getPawnsOnTheWay(gameState: GameState, move: PawnMove): Map[PawnPosition, Pawn] = {
    val deltaX = move.to.x - move.from.x
    val deltaY = move.to.y - move.from.y

    (for {
      dx           <- if (deltaX > 0) List.range(1, deltaX) else List.range(deltaX, 0)
      dy           <- if (deltaY > 0) List.range(1, deltaY) else List.range(deltaY, 0)
      if dx.abs == dy.abs
      pawnPosition <- PawnPosition(move.from.x + dx, move.from.y + dy)
      pawn          = gameState.board.pawnAt(pawnPosition)
      if pawn.isDefined
    } yield pawnPosition -> pawn.get).toMap

  }

  def getBoardAfterMove(gameState: GameState, move: PawnMove): Board = {
    val pawn: Pawn                        = gameState.board.pawnAt(move.from).orNull
    val smashedPawnPosition: PawnPosition = getSmashedPawn(gameState, move).map(_._1).orNull

    Board(
      gameState.board.pawns
        - move.from
        - smashedPawnPosition
        + (move.to -> pawn)
    )
  }

  def checkNextToSmash(gameState: GameState, move: PawnMove): Boolean = {
    val newBoard: Board = getBoardAfterMove(gameState, move)

    val newState: GameState = GameState(
      status = GameStatus.Ongoing,
      movesNow = gameState.movesNow,
      board = newBoard,
      nextMoveFrom = if (newBoard.pawnAt(move.to).isDefined) Some(move.to) else None
    )

    isSthToSmash(newState)
  }

  def isBlocked(board: Board, side: Side): Boolean = {
    val tempState = GameState(status = GameStatus.Ongoing, movesNow = side, board = board, nextMoveFrom = None)

    if (isSthToSmash(tempState))
      false //player has sth to smash, so it is not blocked
    else { //nothing to smash - check if it is possible to move without smash
      val availablePositions =
        for {
          pawn      <- board.pawns.filter(_._2.side == side)
          delta     <- List(-1, 1)
          positions <- if (pawn._2.pawnType == Queen)
                         List(
                           PawnPosition(pawn._1.x + 1, pawn._1.y + delta),
                           PawnPosition(pawn._1.x - 1, pawn._1.y + delta)
                         )
                       else if (pawn._2.side == White)
                         List(PawnPosition(pawn._1.x - 1, pawn._1.y + delta))
                       else
                         List(PawnPosition(pawn._1.x + 1, pawn._1.y + delta))
          position  <- positions
          if board.positionIsAvailable(position)
        } yield position

      availablePositions.isEmpty //no available positions to move, player is blocked
    }
  }

  def checkNewStatus(
    boardAfterMove: Board,
    movesNow: Side,
    nextMoveType: PawnMoveType
  ): GameStatus =
    nextMoveType match {
      case Single    =>
        if (isBlocked(boardAfterMove, movesNow.opposite))
          GameStatus.makeWinner(movesNow)
        else
          GameStatus.Ongoing

      case WithSmash =>
        if (!boardAfterMove.pawns.exists(_._2.side == movesNow.opposite))
          GameStatus.makeWinner(movesNow)
        else
          GameStatus.Ongoing
    }

}
