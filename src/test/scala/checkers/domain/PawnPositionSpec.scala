package checkers.domain

import org.scalatest.Inspectors.forAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalacheck.Arbitrary._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class PawnPositionSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks {

  it should "return PawnPosition for index between 0 and 31" in {
    List.range(0, 32).exists(PawnPosition.fromIndex(_).isDefined) shouldEqual true
  }

  it should "not return PawnPosition for index < 0" in {
    forAll { a: Int => if (a < 0)
      PawnPosition.fromIndex(a).isDefined shouldEqual false
    }
  }

  it should "not return PawnPosition for index > 31" in {
    forAll { a: Int => if (a > 31)
      PawnPosition.fromIndex(a).isDefined shouldEqual false
    }
  }

}
