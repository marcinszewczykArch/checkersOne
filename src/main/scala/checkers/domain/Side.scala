package checkers.domain

import enumeratum._

sealed abstract class Side(val tag: String) extends EnumEntry {
  import Side._

  def opposite: Side = this match {
    case White => Red
    case Red => White
  }
}

object Side extends Enum[Side] {
  val values: IndexedSeq[Side] = findValues

  case object White extends Side("w")
  case object Red extends Side("r")

  def fromString(side: String): Side = side match {
    case "w" => Side.White
    case "r" => Side.Red
  }
}
