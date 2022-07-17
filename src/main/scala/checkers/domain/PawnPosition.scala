package checkers.domain

import checkers.domain.PawnPosition.availablePositions

final case class PawnPosition(x: Int, y: Int) { //todo: validation x+y is odd number && 8>x>=0 && 8>y>=0

  def isOnTheBoard: Boolean = availablePositions.contains(this)

}

object PawnPosition {
  def fromIndex(index: Int): Option[PawnPosition] = availablePositions.lift(index)

  def toIndex(position: PawnPosition): Int = availablePositions.indexOf(position)

  def availablePositions: List[PawnPosition] =
    List(
      PawnPosition(0, 1), //0
      PawnPosition(0, 3), //1
      PawnPosition(0, 5), //2
      PawnPosition(0, 7), //3
      PawnPosition(1, 0), //4
      PawnPosition(1, 2), //5
      PawnPosition(1, 4), //6
      PawnPosition(1, 6), //7
      PawnPosition(2, 1), //8
      PawnPosition(2, 3), //9
      PawnPosition(2, 5), //10
      PawnPosition(2, 7), //11
      PawnPosition(3, 0), //12
      PawnPosition(3, 2), //13
      PawnPosition(3, 4), //14
      PawnPosition(3, 6), //15
      PawnPosition(4, 1), //16
      PawnPosition(4, 3), //17
      PawnPosition(4, 5), //18
      PawnPosition(4, 7), //19
      PawnPosition(5, 0), //20
      PawnPosition(5, 2), //21
      PawnPosition(5, 4), //22
      PawnPosition(5, 6), //23
      PawnPosition(6, 1), //24
      PawnPosition(6, 3), //25
      PawnPosition(6, 5), //26
      PawnPosition(6, 7), //27
      PawnPosition(7, 0), //28
      PawnPosition(7, 2), //29
      PawnPosition(7, 4), //30
      PawnPosition(7, 6)  //31
    )
}
