package domain

import domain.EColour.EColour

import scala.util.Try

case class GameState(board: Array[Pawn], colour: EColour) {

  def getNewState(move: PawnMove): GameState = {
//
//    val oldPawn: Pawn = board
//      .filter(_.colour == colour) // this is not necessary after validation
//      .filter(_.position == move.from)
//      .head


    val oldPawn: Option[Pawn] = findPawn(move.from, colour)

//    val newPawn = {
//      val position = PawnPosition(move.to.x, move.to.y)
//      Pawn(oldPawn.colour, position)
//    }

    val newPawn: Option[Pawn] = oldPawn.map(o => Pawn(o.colour, move.to))

    val smashedPawn: Option[Pawn] = Try(getSmashedPawn(move)).toOption

    val newBoard: Array[Pawn] = board
      .filterNot(_ == oldPawn.orNull)
      .filterNot(_ == smashedPawn.orNull)
      .appended(newPawn.orNull)

    val newRound: EColour = checkNewRound(move)

    GameState(newBoard, newRound)
  }

  def findPawn(position: PawnPosition, colour: EColour): Option[Pawn] = {
    board.find(o => o.position == position && o.colour == colour)
  }

  def pawnExists(position: PawnPosition, colour: EColour): Boolean = findPawn(position, colour).isDefined

  def positionIsAvailable(position: PawnPosition): Boolean = !board.exists(_.position == position) && position.isOnTheBoard

  //todo: case _
  def otherColour(): EColour = colour match {
    case EColour.r => EColour.w
    case EColour.w => EColour.r
  }

  //todo: to be moved to service
  def isNextToSmash(move: PawnMove) = {
    val tx = move.to.x
    val ty = move.to.y

    val otherColour: EColour = this.otherColour()

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

  def checkNewRound(move: PawnMove): EColour = if (isNextToSmash(move)) {
    colour //todo: if there is another pawn to smash we should include in state, that the same pawn should be used
  } else {
    this.otherColour()
  }

}
