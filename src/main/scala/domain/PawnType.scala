package domain

import enumeratum._

sealed abstract class PawnType (val tag: String) extends EnumEntry

object PawnType extends Enum[PawnType] {
  val values: IndexedSeq[PawnType] = findValues

  case object Regular extends PawnType("R")
  case object Queen extends PawnType("Q")
}

