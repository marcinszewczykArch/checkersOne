package checkers.domain

import checkers.domain.Side.{Red, White}
import enumeratum._

sealed abstract class PawnType (val tag: String) extends EnumEntry

object PawnType extends Enum[PawnType] {
  val values: IndexedSeq[PawnType] = findValues

  case object Regular extends PawnType("R")
  case object Queen extends PawnType("Q")

  def fromString(side: String): PawnType = side match {
    case "r" => Regular
    case "w" => Regular
    case "R" => Queen
    case "W" => Queen
  }
}

