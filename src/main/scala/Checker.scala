//import EColour.EColour
//
//object Checker extends App{
//
//  val initialState: domain.GameState = {
//    val wPawns: Set[domain.Pawn] = Set(
//      domain.Pawn(EColour.w, domain.PawnPosition(7, 0)),
//      domain.Pawn(EColour.w, domain.PawnPosition(7, 2)),
//      domain.Pawn(EColour.w, domain.PawnPosition(7, 4)),
//      domain.Pawn(EColour.w, domain.PawnPosition(7, 6)),
//
//      domain.Pawn(EColour.w, domain.PawnPosition(6, 1)),
//      domain.Pawn(EColour.w, domain.PawnPosition(6, 3)),
//      domain.Pawn(EColour.w, domain.PawnPosition(6, 5)),
//      domain.Pawn(EColour.w, domain.PawnPosition(6, 7)),
//
//      domain.Pawn(EColour.w, domain.PawnPosition(5, 0)),
//      domain.Pawn(EColour.w, domain.PawnPosition(5, 2)),
//      domain.Pawn(EColour.w, domain.PawnPosition(5, 4)),
//      domain.Pawn(EColour.w, domain.PawnPosition(5, 6))
//    )
//    val rPawns: Set[domain.Pawn] = Set(
//      domain.Pawn(EColour.r, domain.PawnPosition(0, 1)),
//      domain.Pawn(EColour.r, domain.PawnPosition(0, 3)),
//      domain.Pawn(EColour.r, domain.PawnPosition(0, 5)),
//      domain.Pawn(EColour.r, domain.PawnPosition(0, 7)),
//
//      domain.Pawn(EColour.r, domain.PawnPosition(1, 0)),
//      domain.Pawn(EColour.r, domain.PawnPosition(1, 2)),
//      domain.Pawn(EColour.r, domain.PawnPosition(1, 4)),
//      domain.Pawn(EColour.r, domain.PawnPosition(1, 6)),
//
//      domain.Pawn(EColour.r, domain.PawnPosition(2, 1)),
//      domain.Pawn(EColour.r, domain.PawnPosition(2, 3)),
//      domain.Pawn(EColour.r, domain.PawnPosition(2, 5)),
//      domain.Pawn(EColour.r, domain.PawnPosition(2, 7))
//    )
//    val allPawns: Set[domain.Pawn] = wPawns.concat(rPawns)
//
//    domain.GameState(allPawns, EColour.w)
//  }
//
//  private def readFromUserFrom: domain.PawnPosition = {
//    println("fromX:")
//    val fromX = scala.io.StdIn.readInt()
//    println("fromY:")
//    val fromY = scala.io.StdIn.readInt()
//
//    domain.PawnPosition(fromX, fromY)
//  }
//
//  private def readFromUserTo: domain.PawnPosition = {
//
//    println("toX:")
//    val toX = scala.io.StdIn.readInt()
//    println("toY:")
//    val toY = scala.io.StdIn.readInt()
//
//    domain.PawnPosition(toX, toY)
//  }
//
//
//  //game main loop
//    var state = initialState
//  println(state.toFEN)
//  while (true) {
//    state.printBoard
//    state = state.newState(readFromUserFrom, readFromUserTo)
//  }
//
//
//
//}