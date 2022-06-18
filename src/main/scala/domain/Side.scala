package domain

import enumeratum._

sealed abstract class Side(val tag: String) extends EnumEntry {
  import Side._

  def opposite: Side = this match {
    case White => Black
    case Black => White
  }
}

object Side extends Enum[Side] {
  val values: IndexedSeq[Side] = findValues

  case object White extends Side("w")
  case object Black extends Side("b")
}
