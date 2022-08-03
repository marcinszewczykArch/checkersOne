package checkers.domain

import enumeratum.{Enum, EnumEntry}

//todo: Polecam StringEnum tutaj. I w innych miejscach też.
sealed abstract class PawnMoveType extends EnumEntry

object PawnMoveType extends Enum[PawnMoveType] {
  val values: IndexedSeq[PawnMoveType] = findValues

  case object Single    extends PawnMoveType
  case object WithSmash extends PawnMoveType
}
