package domain

import io.circe.Json
import io.circe.syntax.EncoderOps
import io.circe.generic.auto._

import scala.util.Try

case class GameState(
    status: GameStatus,
    movesNow: Side,
    board: Board,
    nextMoveBy: Option[Pawn]
  ) {

  //todo: to produce Option
  def getNewState(move: PawnMove): GameState = {

    //todo: add validation

    val oldPawn: Option[Pawn] = board.findPawn(move.from, movesNow)

    val newPawn: Option[Pawn] = oldPawn.map(o => Pawn(o.side, o.pawnType, move.to))

    val smashedPawn: Option[Pawn] = Try(getSmashedPawn(move)).toOption

    val newBoard: Board = Board(
      board
        .pawnsArray
        .filterNot(_ == oldPawn.orNull)
        .filterNot(_ == smashedPawn.orNull)
        .appended(newPawn.orNull))

    val newRound: Side = checkNewRound(move)

    val newStatus: GameStatus = {
      if (newBoard.pawnsArray.filter(_.side == Side.White).isEmpty)
        GameStatus.Win(Side.Red)
      else if (newBoard.pawnsArray.filter(_.side == Side.Red).isEmpty)
        GameStatus.Win(Side.White)
//    else if (15 moves with queen without smashing) todo: add
//      GameStatus.Draw
      else
        GameStatus.Ongoing
    }

    val newNextMoveBy: Option[Pawn] = {
      if (newRound == movesNow)
        newBoard.pieceAt(move.to)
      else
        None
    }

    GameState(newStatus, newRound, newBoard, newNextMoveBy)
  }

  def isNextToSmash(move: PawnMove) = {
    val tx = move.to.x
    val ty = move.to.y

    val otherColour: Side = movesNow.opposite

    move.to match {
      case o if getSmashedPawn(move) == null => false
      case o if board.pawnExists(PawnPosition(tx + 1, ty - 1), otherColour) && board.positionIsAvailable(PawnPosition(tx + 2, ty + 2)) => true
      case o if board.pawnExists(PawnPosition(tx + 1, ty - 1), otherColour) && board.positionIsAvailable(PawnPosition(tx + 2, ty - 2)) => true
      case o if board.pawnExists(PawnPosition(tx - 1, ty + 1), otherColour) && board.positionIsAvailable(PawnPosition(tx - 2, ty + 2)) => true
      case o if board.pawnExists(PawnPosition(tx - 1, ty - 1), otherColour) && board.positionIsAvailable(PawnPosition(tx - 2, ty - 2)) => true
      case _ => false
    }
  }

  def getSmashedPawn(move: PawnMove): Pawn = {
    val fx = move.from.x
    val fy = move.from.y

    move.to match {
      case o if move.to == PawnPosition(fx + 2, fy + 2) => board.pawnsArray.filter(o => o.position == PawnPosition(fx + 1, fy + 1)).head
      case o if move.to == PawnPosition(fx + 2, fy - 2) => board.pawnsArray.filter(o => o.position == PawnPosition(fx + 1, fy - 1)).head
      case o if move.to == PawnPosition(fx - 2, fy + 2) => board.pawnsArray.filter(o => o.position == PawnPosition(fx - 1, fy + 1)).head
      case o if move.to == PawnPosition(fx - 2, fy - 2) => board.pawnsArray.filter(o => o.position == PawnPosition(fx - 1, fy - 1)).head
      case _ => null
    }
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
        (p.position == o.position.upRight()     && this.board.positionIsAvailable(p.position.upRight()))   ||
          (p.position == o.position.upLeft()    && this.board.positionIsAvailable(p.position.upLeft()))    ||
          (p.position == o.position.downRight() && this.board.positionIsAvailable(p.position.downRight())) ||
          (p.position == o.position.downLeft()  && this.board.positionIsAvailable(p.position.downLeft())))
    ))
  }

  def validateMove(move: PawnMove): Either[MoveValidationError, GameState] = {

    val fx = move.from.x
    val fy = move.from.y
    val tx = move.to.x
    val ty = move.to.y

    val moveType: Either[MoveValidationError, PawnMoveType] = {
      if
        (!this.board.pawnExists(move.from, this.movesNow))    Left(MoveValidationError.WrongPawnColor)
      else if
        (!this.board.positionIsAvailable(move.to))            Left(MoveValidationError.DestinationNotAvailable)
      else if
        (move.from == move.to)                                Left(MoveValidationError.IdenticalStartAndDestinationPosition)
      else if
        (nextMoveBy == board.pieceAt(move.from))              Left(MoveValidationError.ContinueMultipleSmashing)

      else if ((tx, ty) == (fx - 1, fy + 1) &&
        this.movesNow == Side.White)                          Right(PawnMoveType.Single)
      else if ((tx, ty) == (fx - 1, fy - 1) &&
        this.movesNow == Side.White)                          Right(PawnMoveType.Single)
      else if ((tx, ty) == (fx + 1, fy + 1) &&
        this.movesNow == Side.Red)                            Right(PawnMoveType.Single)
      else if ((tx, ty) == (fx + 1, fy - 1) &&
        this.movesNow == Side.Red)                            Right(PawnMoveType.Single)

      else if ((tx, ty) == (fx + 2, fy + 2) &&
        this.board.pawnExists(PawnPosition(move.from.x + 1, move.from.y + 1), this.movesNow.opposite))  Right(PawnMoveType.WithSmash)
      else if ((tx, ty) == (fx - 2, fy - 2) &&
        this.board.pawnExists(PawnPosition(move.from.x - 1, move.from.y - 1), this.movesNow.opposite))  Right(PawnMoveType.WithSmash)
      else if ((tx, ty) == (fx + 2, fy - 2) &&
        this.board.pawnExists(PawnPosition(move.from.x + 1, move.from.y - 1), this.movesNow.opposite))  Right(PawnMoveType.WithSmash)
      else if ((tx, ty) == (fx - 2, fy + 2) &&
        this.board.pawnExists(PawnPosition(move.from.x - 1, move.from.y + 1), this.movesNow.opposite))  Right(PawnMoveType.WithSmash)

      else                                                                                              Left(MoveValidationError.IllegalMove)
    }

    val sthToSmash = this.isSthToSmash

    moveType match {
      case Right(PawnMoveType.Single)     if !sthToSmash  => Right(this.getNewState(move))
      case Right(PawnMoveType.Single)     if sthToSmash   => Left(MoveValidationError.OpponentPawnToTake)
      case Right(PawnMoveType.WithSmash)  if sthToSmash   => Right(this.getNewState(move))
      case Left(error)                                    => Left(error)
      case _                                              => Left(MoveValidationError.IllegalMove)
    }
  }


}


object GameState {
  def initial: GameState =
    GameState(
      status = GameStatus.Ongoing,
      movesNow = Side.White,
      board = Board.initial,
      nextMoveBy = None
    )

  def toJson(state: GameState): Json = {
    case class State(board: String, currentColour: String)

    val boardArray: Array[(Int, Side)] = state.board.pawnsArray.map(o => (PawnPosition.toIndex(o.position), o.side))
    val board: String = (0 to 31)
      .map(n => boardArray
        .find(_._1 == n)
        .map(_._2.tag)
        .getOrElse("o"))
      .mkString("")

    State(board, state.movesNow.tag).asJson
  }

  def fromString(boardString: String, roundString: String): GameState = {

    val board: Array[Pawn] = boardString
      .split("")
      .zipWithIndex
      .filter(o => o._1 != "o")
      .map(o => (Side.fromString(o._1), PawnPosition.fromIndex(o._2)))
      .map(o => Pawn(o._1, PawnType.Regular, o._2))
    //todo: this is always for regular pawn, not for queen - needs to be changed!!!


    val round: Side = roundString match {
      case "r" => Side.Red
      case "w" =>  Side.White
    }

    domain.GameState(GameStatus.Ongoing, round, Board(board), None) //todo: game status and nextMoveBy to be added!!!
  }
}
