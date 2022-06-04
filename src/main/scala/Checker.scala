import EColour.EColour

object Checker extends App{

  val initialState: GameState = {
    val wPawns: Set[Pawn] = Set(
      Pawn(EColour.w, PawnPosition(7, 0)),
      Pawn(EColour.w, PawnPosition(7, 2)),
      Pawn(EColour.w, PawnPosition(7, 4)),
      Pawn(EColour.w, PawnPosition(7, 6)),

      Pawn(EColour.w, PawnPosition(6, 1)),
      Pawn(EColour.w, PawnPosition(6, 3)),
      Pawn(EColour.w, PawnPosition(6, 5)),
      Pawn(EColour.w, PawnPosition(6, 7)),

      Pawn(EColour.w, PawnPosition(5, 0)),
      Pawn(EColour.w, PawnPosition(5, 2)),
      Pawn(EColour.w, PawnPosition(5, 4)),
      Pawn(EColour.w, PawnPosition(5, 6))
    )
    val rPawns: Set[Pawn] = Set(
      Pawn(EColour.r, PawnPosition(0, 1)),
      Pawn(EColour.r, PawnPosition(0, 3)),
      Pawn(EColour.r, PawnPosition(0, 5)),
      Pawn(EColour.r, PawnPosition(0, 7)),

      Pawn(EColour.r, PawnPosition(1, 0)),
      Pawn(EColour.r, PawnPosition(1, 2)),
      Pawn(EColour.r, PawnPosition(1, 4)),
      Pawn(EColour.r, PawnPosition(1, 6)),

      Pawn(EColour.r, PawnPosition(2, 1)),
      Pawn(EColour.r, PawnPosition(2, 3)),
      Pawn(EColour.r, PawnPosition(2, 5)),
      Pawn(EColour.r, PawnPosition(2, 7))
    )
    val allPawns: Set[Pawn] = wPawns.concat(rPawns)

    GameState(allPawns, EColour.w)
  }

  private def readFromUserFrom: PawnPosition = {
    println("fromX:")
    val fromX = scala.io.StdIn.readInt()
    println("fromY:")
    val fromY = scala.io.StdIn.readInt()

    PawnPosition(fromX, fromY)
  }

  private def readFromUserTo: PawnPosition = {

    println("toX:")
    val toX = scala.io.StdIn.readInt()
    println("toY:")
    val toY = scala.io.StdIn.readInt()

    PawnPosition(toX, toY)
  }


  //game main loop
    var state = initialState
  println(state.toFEN)
  while (true) {
    state.printBoard
    state = state.newState(readFromUserFrom, readFromUserTo)
  }



}