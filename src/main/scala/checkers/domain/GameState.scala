package checkers.domain

import checkers.domain.Board.EMPTY_POSITION
import checkers.domain.Side.{Red, White}

final case class GameState(
    status: GameStatus = GameStatus.Ongoing,
    movesNow: Side,
    board: Board,
    nextMoveBy: Option[Pawn] = None
  ) {

  //todo: to produce Option
  def getNewState(move: PawnMove): GameState = {

    val oldPawn: Option[Pawn] = board.pawnAt(move.from)

    val newPawn: Option[Pawn] = oldPawn match {
      case None       => None
      case Some(pawn) =>
        val newSide     = pawn.side
        val newPosition = move.to
        //todo: if there is another pawn to smash pawnType can not become a queen! Here we should assume the pawnType remains unchanged.
        val newType     = pawn.pawnType match {
            case PawnType.Queen   => PawnType.Queen
            case PawnType.Regular => (move.from.x, move.to.x) match {
              case (_, 0) => PawnType.Queen
              case (_, 7) => PawnType.Queen
              case _      => PawnType.Regular
            }
        }

      Some(Pawn(newSide, newType, newPosition))
    }

    val smashedPawn: Option[Pawn] = getSmashedPawn(move)

    val newBoard: Board = Board(
      board
        .pawnsArray
        .filterNot(_ == oldPawn.orNull)
        .filterNot(_ == smashedPawn.orNull)
        .appended(newPawn.orNull))



    val newRound: Side = checkNewRound(move)

    val newStatus: GameStatus = {
      if (!newBoard.pawnsArray.exists(_.side == White))
        GameStatus.Win(Red)
      else if (!newBoard.pawnsArray.exists(_.side == Red))
        GameStatus.Win(White)
//    else if (15 moves with queen without smashing) todo: add
//      GameStatus.Draw
      else
        GameStatus.Ongoing
    }

    val newNextMoveBy: Option[Pawn] = {
      if (newRound == movesNow) {
        newBoard.pawnAt(move.to)
      } else
      //todo: here check if newPawn should change to queen
        None
    }

    GameState(newStatus, newRound, newBoard, newNextMoveBy)
  }

  def isNextToSmash(move: PawnMove): Boolean = {
    val otherColour: Side = movesNow.opposite

    if (getSmashedPawn(move).isEmpty)
      false
    else if (board.pawnExists(move.to.upRight(),   otherColour) && board.positionIsAvailable(move.to.doubleUpRight()))
      true
    else if (board.pawnExists(move.to.upLeft(),    otherColour) && board.positionIsAvailable(move.to.doubleUpLeft()))
      true
    else if (board.pawnExists(move.to.downRight(), otherColour) && board.positionIsAvailable(move.to.doubleDownRight()))
      true
    else if (board.pawnExists(move.to.downLeft(),  otherColour) && board.positionIsAvailable(move.to.doubleDownLeft()))
      true
    else
      false
  }

  def getSmashedPawn(move: PawnMove): Option[Pawn] = {
    if (move.to == move.from.doubleUpRight())
      board.pawnAt(move.from.upRight())
    else if (move.to == move.from.doubleDownRight())
    board.pawnAt(move.from.downRight())
    else if  (move.to == move.from.doubleUpLeft())
      board.pawnAt(move.from.upLeft())
    else if  (move.to == move.from.doubleDownLeft())
      board.pawnAt(move.from.downLeft())
    else
      None
  }

  def checkNewRound(move: PawnMove): Side = {
    if (isNextToSmash(move))
      movesNow //todo: if there is another pawn to smash we should include in state, that the same pawn should be used
    else
      movesNow.opposite
  }

  def isSthToSmash: Boolean = {
    val colour: Side = this.movesNow
    val board: Board = this.board

    board.pawnsArray.filter(_.side == colour).exists(o => board.pawnsArray.exists(p =>
      p.side != colour && (
          (p.position == o.position.upRight()   && this.board.positionIsAvailable(p.position.upRight()))   ||
          (p.position == o.position.upLeft()    && this.board.positionIsAvailable(p.position.upLeft()))    ||
          (p.position == o.position.downRight() && this.board.positionIsAvailable(p.position.downRight())) ||
          (p.position == o.position.downLeft()  && this.board.positionIsAvailable(p.position.downLeft())))
    ))
  }
}

object GameState {
  def initial: GameState =
    GameState(
      status = GameStatus.Ongoing,
      movesNow = White,
      board = Board.initial,
      nextMoveBy = None
    )

  def fromString(boardString: String, roundString: String): GameState = {

    val board: Array[Pawn] = boardString
      .split("")
      .zipWithIndex
      .filter(o => o._1 != EMPTY_POSITION)
      .map(o => (Side.fromString(o._1), PawnType.fromString(o._1), PawnPosition.fromIndex(o._2)))
      .map(o => Pawn(o._1, o._2, o._3))

    val round: Side = roundString match {
      case "r" => Red
      case "R" => Red
      case "w" => White
      case "W" => White
    }

    GameState(
      movesNow = round,
      board = Board(board)
    ) //todo: game status and nextMoveBy are by default
  }
}
