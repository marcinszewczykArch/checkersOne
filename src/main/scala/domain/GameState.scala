package domain

import domain.EColour.EColour

case class GameState(board: Array[Pawn], round: EColour) {

  def getNewState(move: PawnMove): GameState = {

    val oldPawn: Pawn = board
      .filter(_.colour == round) // this is not necessary after validation
      .filter(_.pawnPosition == move.from)
      .head

    val newPawn: Pawn = {
      val position = PawnPosition(move.to.x, move.to.y)
      Pawn(oldPawn.colour, position)
    }

    val fx = move.from.x
    val fy = move.from.y
    val smashedPawn: Pawn = move.to match {
      case o if move.to == PawnPosition(fx + 2, fy + 2) => board.filter(o => o.pawnPosition == PawnPosition(fx + 1, fy + 1)).head
      case o if move.to == PawnPosition(fx + 2, fy - 2) => board.filter(o => o.pawnPosition == PawnPosition(fx + 1, fy - 1)).head
      case o if move.to == PawnPosition(fx - 2, fy + 2) => board.filter(o => o.pawnPosition == PawnPosition(fx - 1, fy + 1)).head
      case o if move.to == PawnPosition(fx - 2, fy - 2) => board.filter(o => o.pawnPosition == PawnPosition(fx - 1, fy - 1)).head
      case _ => null
    }

    //newBoard //todo: this is totally shit
    var newBoard: Array[Pawn] = null
    //    val newBoard2: Array[Pawn] = board.filter(_ != oldPawn).filter(_ != smashedPawn) :+ newPawn
    if (smashedPawn != null) {
      newBoard = board
        .updated(board.indexOf(oldPawn), Pawn(EColour.o, oldPawn.pawnPosition)) //wyczyść aktualną pozycję
        .updated(board.indexOf(board.filter(_.pawnPosition == newPawn.pawnPosition).head), newPawn) //zmień puste pole na nowego pionka
        .filter(_ != smashedPawn) :+ Pawn(EColour.o, smashedPawn.pawnPosition)
    } else {
      newBoard = board
        .updated(board.indexOf(oldPawn), Pawn(EColour.o, oldPawn.pawnPosition)) //wyczyść aktualną pozycję
        .updated(board.indexOf(board.filter(_.pawnPosition == newPawn.pawnPosition).head), newPawn) //zmień puste pole na nowego pionka
    }


    val tx = move.to.x
    val ty = move.to.y
    val isNextToSmash = {
      val colour: EColour = round
      val otherColour: EColour = if (colour == EColour.w || colour == EColour.W) EColour.r else if (colour == EColour.r || colour == EColour.R) EColour.w else EColour.o

      move.to match {
        case o if smashedPawn == null => false
        case o if
          board.exists(o => o.pawnPosition == PawnPosition(tx + 1, ty + 1) && o.colour == otherColour) &&
          board.exists(o => o.pawnPosition == PawnPosition(tx + 2, ty + 2) && o.colour == EColour.o) => true
        case o if
          board.exists(o => o.pawnPosition == PawnPosition(tx + 1, ty - 1) && o.colour == otherColour) &&
          board.exists(o => o.pawnPosition == PawnPosition(tx + 2, ty - 2) && o.colour == EColour.o) => true
        case o if
          board.exists(o => o.pawnPosition == PawnPosition(tx - 1, ty + 1) && o.colour == otherColour) &&
          board.exists(o => o.pawnPosition == PawnPosition(tx - 2, ty + 2) && o.colour == EColour.o) => true
        case o if
          board.exists(o => o.pawnPosition == PawnPosition(tx - 1, ty - 1) && o.colour == otherColour) &&
          board.exists(o => o.pawnPosition == PawnPosition(tx - 2, ty - 2) && o.colour == EColour.o) => true
        case _ => false
    }
  }


    val newRound: EColour = isNextToSmash match {
      case false  => if (round == EColour.r) EColour.w else EColour.r
      case true   => round
    }

    GameState(newBoard, newRound)
  }

}
