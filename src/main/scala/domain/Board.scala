package domain

import domain.EPawnColour.EColour

final case class Board (pawnsMap: Map[PawnPosition, Pawn]) {

  def pieceAt(position: PawnPosition): Option[Pawn] = pawnsMap.get(position)

  def findPawn(position: PawnPosition, colour: EColour): Option[Pawn] = pieceAt(position).filter(_.colour == colour)

  def pawnExists(position: PawnPosition, colour: EColour): Boolean = findPawn(position, colour).isDefined

  def positionIsAvailable(position: PawnPosition): Boolean = !board.exists(_.position == position) && position.isOnTheBoard

  object Board {
    def initial: Board = {
      ???    //todo add initial state of board
    }
  }

}