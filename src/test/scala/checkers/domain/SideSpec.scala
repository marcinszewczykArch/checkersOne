package checkers.domain

import checkers.domain.Side._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class SideSpec extends AnyFlatSpec with should.Matchers {

  "Side opposite" should "correctly returns the opposite side" in {
    Red.opposite shouldEqual White
    White.opposite shouldEqual Red
  }

  "pawn char" should "correctly returns the pawn side" in {
    Side.fromString("r") shouldEqual Some(Red)
    Side.fromString("R") shouldEqual Some(Red)
    Side.fromString("w") shouldEqual Some(White)
    Side.fromString("W") shouldEqual Some(White)
    Side.fromString("o") shouldEqual None
  }

}
