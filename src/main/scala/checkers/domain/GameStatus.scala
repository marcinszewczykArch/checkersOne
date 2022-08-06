package checkers.domain

import checkers.domain.Side.{Red, White}
import enumeratum._
import enumeratum.values.{StringEnum, StringEnumEntry}

//sealed abstract class GameStatus(val tag: String) extends EnumEntry

//todo: Polecam StringEnum tutaj. I w innych miejscach też.
sealed abstract class GameStatus(val value: String) extends StringEnumEntry {
  def tag: String = value
}

object GameStatus extends StringEnum[GameStatus] {
  val values: IndexedSeq[GameStatus] = findValues

//todo: Wtedy tę metodę można wyrzucic :) ms: replaced by GameStatus.withValueOpt(string)
  // mam wątpliwości czy tak jest lepiej, ponieważ teraz we wszystkich wywołaniach trzeba dodawać .toLowerCase (wcześniej było w jednym miejscu)

//  def fromString(status: String): Option[GameStatus] =
//    status.toLowerCase match {
//      case Ongoing.tag  => Some(Ongoing)
//      case WinRed.tag   => Some(WinRed)
//      case WinWhite.tag => Some(WinWhite)
//      case Draw.tag     => Some(Draw)
//      case _            => None
//    }

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
