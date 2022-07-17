package checkers.domain

import cats.implicits._
import checkers.domain.Board.EMPTY_POSITION
import checkers.domain.PawnPosition.{availablePositions, toIndex}
import checkers.domain.PawnType.Regular
import checkers.domain.Side.{Red, White}

final case class Board(pawnsList: List[Pawn]) {

  def positionIsAvailable(position: PawnPosition): Boolean = pawnAt(position).isEmpty && position.isOnTheBoard

  def pawnAt(position: PawnPosition): Option[Pawn] = pawnsList.find(_.position == position)

  def promoteForQueen(): Board =
    this.pawnsList
      .filter(_.pawnType == Regular)
      .find(o => (o.position.x == 0 && o.side == White) || (o.position.x == 7 && o.side == Red))
      .map(pawnToPromote =>
        Board(
          this.pawnsList
            .filterNot(_ == pawnToPromote)
            .appended(Pawn(pawnToPromote.side, PawnType.Queen, pawnToPromote.position))
        )
      )
      .getOrElse(this)

  override def toString: String = {

    val boardArray: List[(Int, PawnType, Side)] =
      this.pawnsList.map(o => (toIndex(o.position), o.pawnType, o.side))

    availablePositions.indices
      .map(n =>
        boardArray
          .find(_._1 == n)
          .map { o: (Int, PawnType, Side) =>
            if (o._2 == PawnType.Regular)
              o._3.tag
            else
              o._3.tag.toUpperCase()
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
        board
          .split("")
          .zipWithIndex
          .filter(o => o._1 != EMPTY_POSITION)
          .map(o => (Side.fromString(o._1), PawnType.fromString(o._1), PawnPosition.fromIndex(o._2)))
          .map {
            case (Some(side), Some(pawnType), Some(pawnPosition)) => Some(Pawn(side, pawnType, pawnPosition))
            case _                                                => None
          }
          .toList
          .traverse(identity) match {
          case Some(pawns) => Some(Board(pawns))
          case _           => None
        }

      case _  => None
    }

}
