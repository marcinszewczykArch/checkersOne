package checkers.domain

import enumeratum._

sealed abstract class GameStatus(val tag: String) extends EnumEntry

object GameStatus extends Enum[GameStatus] {
  val values: IndexedSeq[GameStatus] = findValues

  case object Ongoing extends GameStatus("ongoing")
  final case class Win(by: Side) extends GameStatus("win")
  case object Draw extends GameStatus("draw")
}