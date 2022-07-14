package checkers.domain

import enumeratum._

sealed abstract class GameStatus(val tag: String) extends EnumEntry

object GameStatus extends Enum[GameStatus] {
  val values: IndexedSeq[GameStatus] = findValues

  def fromString(status: String): Option[GameStatus] =
    status.toLowerCase match {
      case Ongoing.tag  => Some(Ongoing)
      case WinRed.tag   => Some(WinRed)
      case WinWhite.tag => Some(WinWhite)
      case Draw.tag     => Some(Draw)
      case _            => None
    }

  case object Ongoing  extends GameStatus("ongoing")

  case object WinRed   extends GameStatus("winred")

  case object WinWhite extends GameStatus("winwhite")

  case object Draw     extends GameStatus("draw")

}
