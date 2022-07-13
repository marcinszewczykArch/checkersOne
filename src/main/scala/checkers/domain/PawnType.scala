package checkers.domain

import checkers.domain.Side.{Red, White}
import enumeratum._

sealed abstract class PawnType extends EnumEntry

object PawnType extends Enum[PawnType] {
  val values: IndexedSeq[PawnType] = findValues

  case object Regular extends PawnType
  case object Queen extends PawnType

  def fromString(side: String): Option[PawnType] = side match {
    case "r" => Some(Regular)
    case "w" => Some(Regular)
    case "R" => Some(Queen)
    case "W" => Some(Queen)
    case _   => None
  }
}

