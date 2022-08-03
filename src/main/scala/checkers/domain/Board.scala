package checkers.domain

import cats.implicits._
import checkers.domain.Board.EMPTY_POSITION
import checkers.domain.PawnPosition.{availablePositions, toIndex}
import checkers.domain.PawnType.Regular
import checkers.domain.Side.{Red, White}

//todo: Pawns wystarczy. Pawns list podchodzi mi pod notację węgierską.
//todo: Również czy zakładamy sytuację że plansza może by pusta? Jeżeli nie to pomyślałbym o NonEmptyList
//todo: Jeżeli w ogóle zakładamy że Pawn może okupowa jedno unikalne pole, to pomyślałbym o (NonEmpty)Mapie
final case class Board(pawns: List[Pawn]) {

  def positionIsAvailable(position: PawnPosition): Boolean = pawnAt(position).isEmpty

  def pawnAt(position: PawnPosition): Option[Pawn] = pawns.find(_.position == position)

  def promoteForQueen(): Board =
    pawns
      .filter(_.pawnType == Regular)
      //todo: Opisz magic numbery.
      .find(o =>
        (o.position.x == PawnPosition.MIN && o.side == White) || (o.position.x == PawnPosition.MAX && o.side == Red)
      )
      .map(pawnToPromote =>
        Board(
          pawns
            .filterNot(_ == pawnToPromote)
            .appended(Pawn(pawnToPromote.side, PawnType.Queen, pawnToPromote.position))
        )
      )
      .getOrElse(this)

  //todo: Co byś powiedział na zrobienie tego przez Show?
  override def toString: String = {

    //todo: Zrób z tego mapę. Będzie to bardziej elegancko wyglądac.
    val boardArray: Map[Int, (PawnType, Side)] =
      pawns.map(o => (toIndex(o.position), (o.pawnType, o.side))).toMap

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
