package checkers.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class GameStatusSpec extends AnyFlatSpec with should.Matchers {

  "Game status from correct String" should "be valid" in {
    GameStatus.values.map(_.tag).foreach { tag =>
      GameStatus.withValueOpt(tag.toLowerCase.toLowerCase).isDefined shouldEqual true
    }
  }

  "Game status from correct String with upper cases" should "be valid" in {
    GameStatus.values.map(_.tag.toUpperCase()).foreach { tag =>
      GameStatus.withValueOpt(tag.toLowerCase).isDefined shouldEqual true
    }
  }

  "Game status from incorrect String" should "not be valid" in {
    GameStatus.withValueOpt("wrongStatus").isDefined shouldEqual false
  }

}
