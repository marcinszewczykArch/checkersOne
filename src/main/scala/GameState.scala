import EColour.EColour

case class GameState(board: Set[Pawn], round: EColour) {

  def newState(from: PawnPosition, to: PawnPosition): GameState = {

    val pawnToMove: Pawn = board
      .filter(_.colour == round)
      .filter(_.PawnPosition.x == from.x)
      .filter(_.PawnPosition.y == from.y)
      .head

    val newPawn: Pawn = {
      val position = PawnPosition(to.x, to.y)
      Pawn(pawnToMove.colour, position)
    }

    val newBoard: Set[Pawn] = board - pawnToMove + newPawn
    val newRound: EColour = {
      if(round == EColour.r) EColour.w
      else EColour.r
    }

    GameState(newBoard, newRound)
  }

  def printBoard: Unit = {
    val array2d = Array.ofDim[Char](8,8)
    board.foreach(o => array2d(o.PawnPosition.x)(o.PawnPosition.y) = o.colour.toString.head)

    println("current round: " + round)
    array2d.map(_.mkString("     ")).foreach(println)
  }

  //todo: improve this converter
  def toFEN: String = {
    val array2d = Array.ofDim[Pawn](8,8)
    board.foreach(o => array2d(o.PawnPosition.x)(o.PawnPosition.y) = o)

    def formatRow(row: Array[Pawn]) : String = {
      val sb = new StringBuilder
      row.map{
        case x if x!=null => x.colour.toString.head
        case _ => "0"
      }
      .foreach(sb.append)
      sb.toString()
        .replaceAll("00000000", "8")
        .replaceAll("0000000", "7")
        .replaceAll("000000", "6")
        .replaceAll("00000", "5")
        .replaceAll("0000", "4")
        .replaceAll("000", "3")
        .replaceAll("00", "2")
        .replaceAll("0", "1")
    }

    val sb2 = new StringBuilder
    array2d.map(formatRow).foreach(o => sb2.append(o + "/"))
    sb2.toString()

  }
}