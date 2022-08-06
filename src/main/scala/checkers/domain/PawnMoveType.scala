package checkers.domain

import enumeratum.{Enum, EnumEntry}

sealed abstract class PawnMoveType extends EnumEntry

object PawnMoveType extends Enum[PawnMoveType] {
  val values: IndexedSeq[PawnMoveType] = findValues

  case object Single    extends PawnMoveType
  case object WithSmash extends PawnMoveType
}
