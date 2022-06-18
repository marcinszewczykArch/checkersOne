package domain

final case class Board (pawnsArray: Array[Pawn]) {

  def pieceAt(position: PawnPosition): Option[Pawn] = pawnsArray.find(_.position == position)

  def findPawn(position: PawnPosition, side: Side): Option[Pawn] = pieceAt(position).filter(_.side == side)

  def pawnExists(position: PawnPosition, side: Side): Boolean = findPawn(position, side).isDefined

  def positionIsAvailable(position: PawnPosition): Boolean = pieceAt(position).isEmpty && position.isOnTheBoard

}

object Board {
  def initial: Board = {
    ???    //todo: add initial state of board
  }
}
