package domain

import domain.EColour.EColour

case class GameState(board: Array[Pawn], colour: EColour) {

  def getNewState(move: PawnMove): GameState = {

    val oldPawn: Pawn = board
      .filter(_.colour == colour) // this is not necessary after validation
      .filter(_.position == move.from)
      .head
    val newPawn: Pawn = {
      val position = PawnPosition(move.to.x, move.to.y)
      Pawn(oldPawn.colour, position)
    }
    val smashedPawn: Pawn = getSmashedPawn(move)

    val newBoard: Array[Pawn] = board.filter(_ != oldPawn).filter(_ != smashedPawn) :+ newPawn
    val newRound: EColour = checkNewRound(move)

    GameState(newBoard, newRound)
  }

  def pawnExists(position: PawnPosition, colour: EColour): Boolean =
    board.exists(o => o.position == position && o.colour == colour)

  def positionIsAvailable(position: PawnPosition): Boolean =
    !board.exists(o => o.position == position) && position.isOnTheBoard

  def otherColour(): EColour = colour match {
    case EColour.r => EColour.w
    case EColour.w => EColour.r
  }

  //todo: to be moved to service
  def isNextToSmash(move: PawnMove) = {
    val tx = move.to.x
    val ty = move.to.y

    val otherColour: EColour = if (colour == EColour.w || colour == EColour.W) EColour.r else EColour.w

    move.to match {
      case o if getSmashedPawn(move) == null => false
      case o if pawnExists(PawnPosition(tx + 1, ty - 1), otherColour) && positionIsAvailable(PawnPosition(tx + 2, ty + 2)) => true
      case o if pawnExists(PawnPosition(tx + 1, ty - 1), otherColour) && positionIsAvailable(PawnPosition(tx + 2, ty - 2)) => true
      case o if pawnExists(PawnPosition(tx - 1, ty + 1), otherColour) && positionIsAvailable(PawnPosition(tx - 2, ty + 2)) => true
      case o if pawnExists(PawnPosition(tx - 1, ty - 1), otherColour) && positionIsAvailable(PawnPosition(tx - 2, ty - 2)) => true
      case _ => false
    }
  }

  def getSmashedPawn(move: PawnMove): Pawn = {
    val fx = move.from.x
    val fy = move.from.y

    move.to match {
    case o if move.to == PawnPosition(fx + 2, fy + 2) => board.filter(o => o.position == PawnPosition(fx + 1, fy + 1)).head
    case o if move.to == PawnPosition(fx + 2, fy - 2) => board.filter(o => o.position == PawnPosition(fx + 1, fy - 1)).head
    case o if move.to == PawnPosition(fx - 2, fy + 2) => board.filter(o => o.position == PawnPosition(fx - 1, fy + 1)).head
    case o if move.to == PawnPosition(fx - 2, fy - 2) => board.filter(o => o.position == PawnPosition(fx - 1, fy - 1)).head
    case _ => null
    }
  }

  def checkNewRound(move: PawnMove): EColour = isNextToSmash(move) match {
    case false  => if (colour == EColour.r) EColour.w else EColour.r
    case true   => colour
  }

}
