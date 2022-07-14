package checkers.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class PawnPositionSpec extends AnyFlatSpec with should.Matchers {

  "indexes from 0 to 31" should "return PawnPosition" in {
    List.range(0, 32).exists(PawnPosition.fromIndex(_).isEmpty) shouldEqual false
  }
  "indexes from 32 to 99" should "NOT return PawnPosition" in {
    List.range(32, 100).exists(PawnPosition.fromIndex(_).isDefined) shouldEqual false
  }
  "indexes from -100 to -1" should "NOT return PawnPosition" in {
    List.range(-100, 0).exists(PawnPosition.fromIndex(_).isDefined) shouldEqual false
  }

}
