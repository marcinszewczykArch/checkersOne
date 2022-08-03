package checkers.domain

import checkers.domain.PawnPosition.{MAX, MIN}

import scala.util.Try

final case class PawnPosition private (x: Int, y: Int) {

  require((x + y) % 2 != 0, "x + y must be odd number")
  require(x >= MIN && x <= MAX, "x must be between 0 and 7")
  require(y >= MIN && y <= MAX, "y must be between 0 and 7")

}

object PawnPosition {
  val MIN = 0
  val MAX = 7

  def apply(x: Int, y: Int): Option[PawnPosition] = Try(new PawnPosition(x, y)).toOption

  def fromIndex(index: Int): Option[PawnPosition] = availablePositions.lift(index)

  def availablePositions: List[PawnPosition] =
    List(
      new PawnPosition(0, 1), //0
      new PawnPosition(0, 3), //1
      new PawnPosition(0, 5), //2
      new PawnPosition(0, 7), //3
      new PawnPosition(1, 0), //4
      new PawnPosition(1, 2), //5
      new PawnPosition(1, 4), //6
      new PawnPosition(1, 6), //7
      new PawnPosition(2, 1), //8
      new PawnPosition(2, 3), //9
      new PawnPosition(2, 5), //10
      new PawnPosition(2, 7), //11
      new PawnPosition(3, 0), //12
      new PawnPosition(3, 2), //13
      new PawnPosition(3, 4), //14
      new PawnPosition(3, 6), //15
      new PawnPosition(4, 1), //16
      new PawnPosition(4, 3), //17
      new PawnPosition(4, 5), //18
      new PawnPosition(4, 7), //19
      new PawnPosition(5, 0), //20
      new PawnPosition(5, 2), //21
      new PawnPosition(5, 4), //22
      new PawnPosition(5, 6), //23
      new PawnPosition(6, 1), //24
      new PawnPosition(6, 3), //25
      new PawnPosition(6, 5), //26
      new PawnPosition(6, 7), //27
      new PawnPosition(7, 0), //28
      new PawnPosition(7, 2), //29
      new PawnPosition(7, 4), //30
      new PawnPosition(7, 6)  //31
    )

  def toIndex(position: PawnPosition): Int = availablePositions.indexOf(position)
}
