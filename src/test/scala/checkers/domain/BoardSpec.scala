package checkers.domain

import checkers.domain.PawnType.Regular
import checkers.domain.Side.{Red, White}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class BoardSpec extends AnyFlatSpec with should.Matchers {

  "Initial board" should "work" in {
    assert(initialBoard equals Board.initial)
  }
  "Initial board toString" should "work" in {
    assert(initialBoardString equals Board.initial.toString)
  }
  "Initial board fromString" should "work" in {
    assert(Board.fromString(initialBoardString).get equals Board.initial)
  }

  "Invalid string with correct length" should "not get Board" in {
    assert(Board.fromString("rrrrrrrrrrrroooXoooowwwwwwwwwwww").isEmpty)
  }

  "Correct string" should "get Board" in {
    assert(Board.fromString("rrrrrrrrrrrroooooooowwwwwwwwwwww").isDefined)
  }

  "Invalid string" should "not get Board" in {
    assert(Board.fromString("invalidString").isEmpty)
  }

  "PawnPosition01" should "give red pawn" in {
    assert(Board.initial.pawnAt(PawnPosition(0, 1).get) contains Pawn(Red, Regular))
  }
  "PawnPosition76" should "give white pawn" in {
    assert(Board.initial.pawnAt(PawnPosition(7, 6).get) contains Pawn(White, Regular))
  }
  "PawnPosition34" should "be empty" in {
    assert(Board.initial.pawnAt(PawnPosition(3, 4).get).isEmpty)
  }
  "PawnPosition33" should "be not available" in {
    assert(PawnPosition(3, 3).isEmpty)
  }
  "PawnPosition34" should "be not available" in {
    assert(Board.initial.positionIsAvailable(PawnPosition(3, 4).get))
  }

  val initialBoard               = new Board(
    Map(
      PawnPosition(0, 1).get -> Pawn(Red, Regular),
      PawnPosition(0, 3).get -> Pawn(Red, Regular),
      PawnPosition(0, 5).get -> Pawn(Red, Regular),
      PawnPosition(0, 7).get -> Pawn(Red, Regular),
      PawnPosition(1, 0).get -> Pawn(Red, Regular),
      PawnPosition(1, 2).get -> Pawn(Red, Regular),
      PawnPosition(1, 4).get -> Pawn(Red, Regular),
      PawnPosition(1, 6).get -> Pawn(Red, Regular),
      PawnPosition(2, 1).get -> Pawn(Red, Regular),
      PawnPosition(2, 3).get -> Pawn(Red, Regular),
      PawnPosition(2, 5).get -> Pawn(Red, Regular),
      PawnPosition(2, 7).get -> Pawn(Red, Regular),
      PawnPosition(5, 0).get -> Pawn(White, Regular),
      PawnPosition(5, 2).get -> Pawn(White, Regular),
      PawnPosition(5, 4).get -> Pawn(White, Regular),
      PawnPosition(5, 6).get -> Pawn(White, Regular),
      PawnPosition(6, 1).get -> Pawn(White, Regular),
      PawnPosition(6, 3).get -> Pawn(White, Regular),
      PawnPosition(6, 5).get -> Pawn(White, Regular),
      PawnPosition(6, 7).get -> Pawn(White, Regular),
      PawnPosition(7, 0).get -> Pawn(White, Regular),
      PawnPosition(7, 2).get -> Pawn(White, Regular),
      PawnPosition(7, 4).get -> Pawn(White, Regular),
      PawnPosition(7, 6).get -> Pawn(White, Regular)
    )
  )
  val initialBoardString: String =
    s"""
      r r r r
     r r r r
      r r r r
     o o o o
      o o o o
     w w w w
      w w w w
     w w w w
  """
      .replace(" ", "")
      .replace("\n", "")
      .replace("\r", "")
}
