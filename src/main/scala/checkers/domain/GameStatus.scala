package checkers.domain

import checkers.domain.Side.{Red, White}
import enumeratum.values.{StringEnum, StringEnumEntry}

sealed abstract class GameStatus(val value: String) extends StringEnumEntry {
  def tag: String = value
}

object GameStatus extends StringEnum[GameStatus] {
  val values: IndexedSeq[GameStatus] = findValues

  def makeWinner(side: Side): GameStatus =
    side match {
      case White => WinWhite
      case Red   => WinRed
    }

  case object Ongoing  extends GameStatus("ongoing")
  case object WinRed   extends GameStatus("winred")
  case object WinWhite extends GameStatus("winwhite")
  case object Draw     extends GameStatus("draw")

}
