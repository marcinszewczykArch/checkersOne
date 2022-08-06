package checkers.domain

import enumeratum._

sealed abstract class Side(val tag: String) extends EnumEntry {
  import Side._

  def opposite: Side =
    this match {
      case White => Red
      case Red   => White
    }
}

object Side extends Enum[Side] {
  val values: IndexedSeq[Side] = findValues

  def fromString(side: String): Option[Side] =
    side.toLowerCase match {
      case White.tag => Some(White)
      case Red.tag   => Some(Red)
      case _         => None
    }

  case object White extends Side("w")
  case object Red   extends Side("r")
}
