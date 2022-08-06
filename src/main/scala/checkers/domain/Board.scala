package checkers.domain

import checkers.domain.Board.EMPTY_POSITION
import checkers.domain.PawnPosition._
import checkers.domain.PawnType.{Queen, Regular}
import checkers.domain.Side.{Red, White}

import scala.util.Try

final case class Board(pawns: Map[PawnPosition, Pawn]) {

  def positionIsAvailable(position: PawnPosition): Boolean = pawnAt(position).isEmpty

  def pawnAt(position: PawnPosition): Option[Pawn] = pawns.get(position)

  def promoteForQueen(): Board =
    pawns
      .filter(_._2.pawnType == Regular)
      .find(o => (o._1.x == MIN && o._2.side == White) || (o._1.x == MAX && o._2.side == Red))
      .map(o => Board(pawns + (o._1 -> Pawn(o._2.side, Queen))))
      .getOrElse(this)

  //todo: Co byś powiedział na zrobienie tego przez Show?
  override def toString: String = {

    val boardArray: Map[Int, (PawnType, Side)] =
      pawns.map(o => (toIndex(o._1), (o._2.pawnType, o._2.side)))

    availablePositions.indices
      .map(n =>
        boardArray
          .get(n)
          .map {
            case (PawnType.Regular, side) => side.tag
            case (_, side)                => side.tag.toUpperCase
          }
          .getOrElse(EMPTY_POSITION)
      )
      .mkString("")
  }

}

object Board {

  final val EMPTY_POSITION = "o"

  def initial: Board = fromString("rrrrrrrrrrrroooooooowwwwwwwwwwww").get

  def fromString(board: String): Option[Board] =
    board.length match {
      case 32 =>
        Try(
          Board(
            board
              .split("")
              .zipWithIndex
              .filter(o => o._1 != EMPTY_POSITION)
              .map { o =>
                val pawnPosition = fromIndex(o._2).get
                val pawnSide     = Side.fromString(o._1).get
                val pawnType     = PawnType.fromString(o._1).get
                pawnPosition -> Pawn(pawnSide, pawnType)
              }
              .toMap
          )
        ).toOption
      case _  => None
    }

}
