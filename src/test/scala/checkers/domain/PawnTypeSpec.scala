package checkers.domain

import checkers.domain.PawnType.{Queen, Regular}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class PawnTypeSpec extends AnyFlatSpec with should.Matchers {

  "pawn char" should "correctly returns the pawn type" in {
    PawnType.fromString("r") shouldEqual Some(Regular)
    PawnType.fromString("w") shouldEqual Some(Regular)
    PawnType.fromString("R") shouldEqual Some(Queen)
    PawnType.fromString("W") shouldEqual Some(Queen)
    PawnType.fromString("o") shouldEqual None
  }

}
