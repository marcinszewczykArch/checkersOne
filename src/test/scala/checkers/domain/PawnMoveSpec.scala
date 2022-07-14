package checkers.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class PawnMoveSpec extends AnyFlatSpec with should.Matchers {

  "indexes from 0 to 31" should "return PawnMove" in {
    for {
      from <- List.range(0, 32)
      to   <- List.range(0, 32)
    } yield PawnMove.fromString(from.toString, to.toString).isDefined shouldEqual true
  }
  "move from 1 to 32" should "NOT return PawnMove" in {
    PawnMove.fromString("1", "32").isDefined shouldEqual false
  }
  "move from -1 to 2" should "NOT return PawnMove" in {
    PawnMove.fromString("-1", "2").isDefined shouldEqual false
  }

}
