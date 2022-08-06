package checkers.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class GameStateSpec extends AnyFlatSpec with should.Matchers {

  "Initial game state" should "be valid" in {
    GameState.fromString(
      board = "rrrrrrrrrrrroooooooowwwwwwwwwwww",
      movesNow = "w",
      nextMoveBy = "None",
      status = "ongoing"
    ).isDefined shouldEqual true
  }

  "Game state with correct nextMoveBy" should "be valid" in {
    GameState.fromString(
      board = "rrrrrrrrrrrroooooooowwwwwwwwwwww",
      movesNow = "w",
      nextMoveBy = "7",
      status = "ongoing"
    ).isDefined shouldEqual true
  }

  "Game state with invalid board" should "not be valid" in {
    GameState.fromString(
      board = "rrrrrrrrroooooooowww",
      movesNow = "w",
      nextMoveBy = "None",
      status = "ongoing"
    ).isDefined shouldEqual false
  }

  "Game state with invalid moves now" should "not be valid" in {
    GameState.fromString(
      board = "rrrrrrrrrrrroooooooowwwwwwwwwwww",
      movesNow = "o",
      nextMoveBy = "None",
      status = "ongoing"
    ).isDefined shouldEqual false
  }

  //todo: this behaviour for nextMoveBy can be simplified
  "Game state with incorrect nextMoveBy" should "be valid and have nextMoveBy not defined" in {
    GameState.fromString(
      board = "rrrrrrrrrrrroooooooowwwwwwwwwwww",
      movesNow = "w",
      nextMoveBy = "33",
      status = "ongoing"
    ).get.nextMoveFrom.isEmpty shouldEqual true
  }

  "Game state with invalid status" should "not be valid" in {
    GameState.fromString(
      board = "rrrrrrrrrrrroooooooowwwwwwwwwwww",
      movesNow = "o",
      nextMoveBy = "None",
      status = "invalidStatus"
    ).isDefined shouldEqual false
  }



}
