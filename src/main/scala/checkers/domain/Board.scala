package checkers.domain

import checkers.domain.Board.EMPTY_POSITION
import checkers.domain.PawnPosition.{availablePositions, toIndex}
import checkers.domain.PawnType.Regular
import checkers.domain.Side.{Red, White}

import scala.util.Try

//todo: Również czy zakładamy sytuację że plansza może by pusta? Jeżeli nie to pomyślałbym o NonEmptyList
//todo: Jeżeli w ogóle zakładamy że Pawn może okupowa jedno unikalne pole, to pomyślałbym o (NonEmpty)Mapie
final case class Board(pawns: Map[PawnPosition, Pawn]) {

  def positionIsAvailable(position: PawnPosition): Boolean = pawnAt(position).isEmpty

  def pawnAt(position: PawnPosition): Option[Pawn] = pawns.get(position)

  def promoteForQueen(): Board =
    pawns
      .filter(_._2.pawnType == Regular)
      .find(o => (o._1.x == PawnPosition.MIN && o._2.side == White) || (o._1.x == PawnPosition.MAX && o._2.side == Red))
      .map((pawnToPromote: (PawnPosition, Pawn)) =>
        Board(
          pawns.filterNot(_ == pawnToPromote)
            ++ Map(pawnToPromote._1 -> Pawn(pawnToPromote._2.side, PawnType.Queen))
        )
      )
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

//  def fromString(board: String): Option[BoardNew] =
//    board.length match {
//      case 32 =>
//        board
//          .split("")
//          .zipWithIndex
//          .filter(o => o._1 != EMPTY_POSITION)
//          .map(o => (Side.fromString(o._1), PawnType.fromString(o._1), PawnPosition.fromIndex(o._2)))
//          .map {
//            case (Some(side), Some(pawnType), Some(pawnPosition)) => Some(pawnPosition -> Pawn(side, pawnType))
//            case _                                                => None
//          }
//          .toList
//          .traverse(identity) match {
//          case Some(pawns) => Some(Board(pawns))
//          case _           => None
//        }
//
//      case _  => None
//    }

  //todo: get rid of .get???
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
                val pawnPosition = PawnPosition.fromIndex(o._2).get
                val pawnSide     = Side.fromString(o._1).get
                val pawnType     = PawnType.fromString(o._1).get
                pawnPosition -> Pawn(pawnSide, pawnType)
              }
              .toMap
          )
        ).toOption
      case _  => None
    }

//  def fromString(board: String): Option[Board] =
//    board.length match {
//      case 32 =>
//        val pawnsWithIndex: Array[(String, Int)] = board
//          .split("")
//          .zipWithIndex
//          .filter(o => o._1 != EMPTY_POSITION)
//
//        val pawnsMap: Map[PawnPosition, Pawn] = (
//          for {
//            pawn: (String, Int)        <- pawnsWithIndex
//            pawnPosition: PawnPosition <- PawnPosition.fromIndex(pawn._2)
//            pawnSide: Side             <- Side.fromString(pawn._1)
//            pawnType: PawnType         <- PawnType.fromString(pawn._1)
//          } yield pawnPosition -> Pawn(pawnSide, pawnType)
//        ).toMap
//
//        Try(Board(pawnsMap)).toOption
//
//      case _  => None
//    }

}
