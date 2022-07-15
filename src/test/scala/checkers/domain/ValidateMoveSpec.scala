package checkers.domain

import checkers.domain.MoveValidationError.{MoveIsNotDiagonal, WrongPawnColor}
import checkers.domain.Side.{Red, White}
import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ValidateMoveSpec extends AnyFlatSpec with should.Matchers {

  //Left(error)
  "Not diagonal single pawn move" should "return Left(MoveIsNotDiagonal)" in {
    val nextMoveBy = None
    val status     = GameStatus.Ongoing
    val movesNow   = White
    val board      = toBoard(s"""
        r r r r
       r r r r
        r r r r
       o o o o
        o o o o
       w w w w
        w w w w
       w w w w
    """)
    val gameState  = GameState(status, movesNow, board, nextMoveBy)
    val pawnMove   = PawnMove.fromString("23", "17").get
    ValidateMove.apply().apply(pawnMove, gameState) shouldEqual Left(MoveIsNotDiagonal)
  }

  "Initial game state with correct move pattern by red pawn" should "return Left(WrongPawnColor)" in {
    val nextMoveBy = None
    val status     = GameStatus.Ongoing
    val movesNow   = White
    val board      = toBoard(s"""
        r r r r
       r r r r
        r r r r
       o o o o
        o o o o
       w w w w
        w w w w
       w w w w
    """)
    val gameState  = GameState(status, movesNow, board, nextMoveBy)
    val pawnMove   = PawnMove.fromString("11", "15").get
    ValidateMove.apply().apply(pawnMove, gameState) shouldEqual Left(WrongPawnColor)
  }

  "Pawn move to not available position" should "return Left(DestinationNotAvailable)" in {}

  "Move from position where there is no pawn" should "return Left(NoPawnAtStartingPosition)" in {}

  "Identical start and destination position" should "return Left(IdenticalStartAndDestinationPosition)" in {}

  "Smashing by other pawn while multiple smashing continues" should "return Left(ContinueMultipleSmashing)" in {}

  "Smashing 2 pawns in one queen move" should "return Left(TooManyPawnsOnTheWay)" in {}

  "Smashing own pawn by queen" should "return Left(SmashingOwnPawnIsNotOk)" in {}

  "Move while game status is different than Ongoing" should "return Left(GameIsOver)" in {}

  "Regular pawn single move while smashing is possible" should "return Left(MoveTypeIsIncorrect)" in {}

  "Queen single move while smashing is possible" should "return Left(MoveTypeIsIncorrect)" in {}

  "Red regular pawn move backward" should "return Left(???)" in {}

  "White regular pawn move backward" should "return Left(???)" in {}

  //todo: check all potential move errors

  //Right(state)
  "Initial game state with correct move pattern by white pawn" should "give correct state" in {
    //initial state
    val nextMoveBy = None
    val status     = GameStatus.Ongoing
    val movesNow   = White
    val board      = toBoard(s"""
        r r r r
       r r r r
        r r r r
       o o o o
        o o o o
       w w w w
        w w w w
       w w w w
    """)
    val gameState  = GameState(status, movesNow, board, nextMoveBy)

    //move
    val pawnMove = PawnMove.fromString("23", "19").get

    //expected state
    val expectedNextMoveBy = None
    val expectedStatus     = GameStatus.Ongoing
    val expectedMovesNow   = Red
    val expectedBoard      = toBoard(s"""
        r r r r
       r r r r
        r r r r
       o o o o
        o o o w
       w w w o
        w w w w
       w w w w
    """)
    val expectedGameState  = GameState(expectedStatus, expectedMovesNow, expectedBoard, expectedNextMoveBy)

    //Assertion
    ValidateMove.apply().apply(pawnMove, gameState) match {
      case Right(actualGameState: GameState) => compareGameStates(actualGameState, expectedGameState)
      case Left(error: MoveValidationError)  => assert(Right == Left(error))
    }
  }

  "Move with single smash" should "give correct state" in {
    //initial state
    val nextMoveBy = None
    val status     = GameStatus.Ongoing
    val movesNow   = White
    val board      = toBoard(s"""
        r o o r
       o o o o
        o r o o
       o w o o
        o o o o
       o o o o
        w o o o
       o o o o
    """)
    val gameState  = GameState(status, movesNow, board, nextMoveBy)

    //move
    val pawnMove = PawnMove.fromString("13", "6").get

    //expected state
    val expectedNextMoveBy = None
    val expectedStatus     = GameStatus.Ongoing
    val expectedMovesNow   = Red
    val expectedBoard      = toBoard(s"""
        r o o r
       o o w o
        o o o o
       o o o o
        o o o o
       o o o o
        w o o o
       o o o o
    """)
    val expectedGameState  = GameState(expectedStatus, expectedMovesNow, expectedBoard, expectedNextMoveBy)

    //Assertion
    ValidateMove.apply().apply(pawnMove, gameState) match {
      case Right(actualGameState: GameState) => compareGameStates(actualGameState, expectedGameState)
      case Left(error: MoveValidationError)  => assert(Right == Left(error))
    }
  }

  "Move with multiple smash" should "give correct state" in {}

  "Ongoing smashing" should "not give a queen" in {}

  "Pawn getting to the board end" should "be promoted to a queen" in {}

  "Smashing last opponent pawn" should "give new state with win-status" in {}

  "Blocking all opponent pawns" should "give new state with win-status" in {}

  "Queen with single smash" should "give correct state" in {}

  "Queen with multiple smash" should "give correct state" in {}

  //helpers
  val toBoard: String => Board = { s =>
    Board
      .fromString(
        s
          .replace(" ", "")
          .replace("\n", "")
          .replace("\r", "")
      )
      .get
  }

  def compareGameStates(actual: GameState, expected: GameState): Assertion = {
    assert(actual.nextMoveBy == expected.nextMoveBy)
    assert(actual.status == expected.status)
    assert(actual.movesNow == expected.movesNow)
    assert(actual.board.toString == expected.board.toString)
    //comparing strings, because order of List[Pawn] inside Board object may differ
  }
}
