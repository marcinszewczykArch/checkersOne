package checkers.domain

import enumeratum._

sealed abstract class PawnType extends EnumEntry

object PawnType extends Enum[PawnType] {
  val values: IndexedSeq[PawnType] = findValues

  def fromString(side: String): Option[PawnType] =
    side match {
      case "r" => Some(Regular)
      case "w" => Some(Regular)
      case "R" => Some(Queen)
      case "W" => Some(Queen)
      case _   => None
    }

  case object Regular extends PawnType
  case object Queen   extends PawnType
}
