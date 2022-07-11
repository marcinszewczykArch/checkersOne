package checkers.domain

import checkers.domain.Board.EMPTY_POSITION
import checkers.domain.PawnPosition.{availablePositions, toIndex}
import checkers.domain.PawnType.Regular
import checkers.domain.Side.{Red, White}

final case class Board (pawnsArray: Array[Pawn]) {

  def pawnAt(position: PawnPosition): Option[Pawn] = pawnsArray.find(_.position == position)

  def findPawn(position: PawnPosition, side: Side): Option[Pawn] = pawnAt(position).filter(_.side == side)

  def pawnExists(position: PawnPosition, side: Side): Boolean = findPawn(position, side).isDefined

  def positionIsAvailable(position: PawnPosition): Boolean = pawnAt(position).isEmpty && position.isOnTheBoard

  def promoteForQueen(): Board = {
    val pawnToPromote: Option[Pawn] = this.pawnsArray
      .filter(_.pawnType == Regular)
      .find(o => o.position.x == 0 && o.side == White || o.position.x == 7 && o.side == Red)

    if (pawnToPromote.isDefined) {
      Board(
        this.pawnsArray
          .filterNot(_ == pawnToPromote.get)
          .appended(Pawn(pawnToPromote.get.side, PawnType.Queen, pawnToPromote.get.position))
      )
    } else {
      this
    }
  }

  override def toString: String = {
    val boardArray: Array[(Int, PawnType, Side)] = this.pawnsArray.map(o => (toIndex(o.position), o.pawnType, o.side))
    availablePositions.indices.map(n =>
      boardArray
        .find(_._1 == n)
        .map{o: (Int, PawnType, Side) =>
          if(o._2 == PawnType.Regular)
            o._3.tag
          else
            o._3.tag.toUpperCase()
        }
        .getOrElse(EMPTY_POSITION)).mkString("")
  }

}

object Board {
  final val EMPTY_POSITION = "o"

  def initial: Board = {
    new Board(
      Array(
        Pawn(White, Regular, PawnPosition(0, 1)),
        Pawn(White, Regular, PawnPosition(0, 3)),
        Pawn(White, Regular, PawnPosition(0, 5)),
        Pawn(White, Regular, PawnPosition(0, 7)),
        Pawn(White, Regular, PawnPosition(1, 0)),
        Pawn(White, Regular, PawnPosition(1, 2)),
        Pawn(White, Regular, PawnPosition(1, 4)),
        Pawn(White, Regular, PawnPosition(1, 6)),
        Pawn(White, Regular, PawnPosition(2, 1)),
        Pawn(White, Regular, PawnPosition(2, 3)),
        Pawn(White, Regular, PawnPosition(2, 5)),
        Pawn(White, Regular, PawnPosition(2, 7)),

        Pawn(Red, Regular, PawnPosition(5, 0)),
        Pawn(Red, Regular, PawnPosition(5, 2)),
        Pawn(Red, Regular, PawnPosition(5, 4)),
        Pawn(Red, Regular, PawnPosition(5, 6)),
        Pawn(Red, Regular, PawnPosition(6, 1)),
        Pawn(Red, Regular, PawnPosition(6, 3)),
        Pawn(Red, Regular, PawnPosition(6, 5)),
        Pawn(Red, Regular, PawnPosition(6, 7)),
        Pawn(Red, Regular, PawnPosition(7, 0)),
        Pawn(Red, Regular, PawnPosition(7, 2)),
        Pawn(Red, Regular, PawnPosition(7, 4)),
        Pawn(Red, Regular, PawnPosition(7, 6))))

  }
}
