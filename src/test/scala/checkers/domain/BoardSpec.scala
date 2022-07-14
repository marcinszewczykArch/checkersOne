package checkers.domain

import checkers.domain.PawnType.Regular
import checkers.domain.Side.{Red, White}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class BoardSpec extends AnyFlatSpec with should.Matchers {

  "Initial board" should "work" in {
    assert(TestData.initialBoard equals Board.initial)
  }
  "Initial board toString" should "work" in {
    assert(TestData.initialBoardString equals Board.initial.toString)
  }
  "Initial board fromString" should "work" in {
    assert(Board.fromString(TestData.initialBoardString).get equals Board.initial)
  }
  "PawnPosition01" should "give red pawn" in {
    assert(Board.initial.pawnAt(PawnPosition(0, 1)) contains Pawn(Red, Regular, PawnPosition(0, 1)))
  }
  "PawnPosition76" should "give white pawn" in {
    assert(Board.initial.pawnAt(PawnPosition(7, 6)) contains Pawn(White, Regular, PawnPosition(7, 6)))
  }
  "PawnPosition34" should "be empty" in {
    assert(Board.initial.pawnAt(PawnPosition(3, 4)).isEmpty)
  }
  "PawnPosition33" should "be empty" in {
    assert(Board.initial.pawnAt(PawnPosition(3, 3)).isEmpty)
  }
  "PawnPosition33" should "be not available" in {
    assert(Board.initial.positionIsAvailable(PawnPosition(3, 3)) == false)
  }
  "PawnPosition34" should "be not available" in {
    assert(Board.initial.positionIsAvailable(PawnPosition(3, 4)) == true)
  }

}

object TestData {

  val initialBoard               = new Board(
    List(
      Pawn(Red, Regular, PawnPosition(0, 1)),
      Pawn(Red, Regular, PawnPosition(0, 3)),
      Pawn(Red, Regular, PawnPosition(0, 5)),
      Pawn(Red, Regular, PawnPosition(0, 7)),
      Pawn(Red, Regular, PawnPosition(1, 0)),
      Pawn(Red, Regular, PawnPosition(1, 2)),
      Pawn(Red, Regular, PawnPosition(1, 4)),
      Pawn(Red, Regular, PawnPosition(1, 6)),
      Pawn(Red, Regular, PawnPosition(2, 1)),
      Pawn(Red, Regular, PawnPosition(2, 3)),
      Pawn(Red, Regular, PawnPosition(2, 5)),
      Pawn(Red, Regular, PawnPosition(2, 7)),
      Pawn(White, Regular, PawnPosition(5, 0)),
      Pawn(White, Regular, PawnPosition(5, 2)),
      Pawn(White, Regular, PawnPosition(5, 4)),
      Pawn(White, Regular, PawnPosition(5, 6)),
      Pawn(White, Regular, PawnPosition(6, 1)),
      Pawn(White, Regular, PawnPosition(6, 3)),
      Pawn(White, Regular, PawnPosition(6, 5)),
      Pawn(White, Regular, PawnPosition(6, 7)),
      Pawn(White, Regular, PawnPosition(7, 0)),
      Pawn(White, Regular, PawnPosition(7, 2)),
      Pawn(White, Regular, PawnPosition(7, 4)),
      Pawn(White, Regular, PawnPosition(7, 6))
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
