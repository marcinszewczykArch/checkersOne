package checkers.domain

import checkers.domain.PawnPosition.availablePositions

final case class PawnPosition(x: Int, y: Int) {

  def isOnTheBoard: Boolean = availablePositions.contains(this)

  //todo: all def's below to be removed
  def doubleUpRight(): PawnPosition = this.upRight().upRight()

  def upRight(): PawnPosition = PawnPosition(x + 1, y + 1) //todo: to return either and left if it is not on the board

  def doubleDownRight(): PawnPosition = this.downRight().downRight()

  def downRight(): PawnPosition = PawnPosition(x + 1, y - 1)

  def doubleUpLeft(): PawnPosition = this.upLeft().upLeft()

  def upLeft(): PawnPosition = PawnPosition(x - 1, y + 1)

  def doubleDownLeft(): PawnPosition = this.downLeft().downLeft()

  def downLeft(): PawnPosition = PawnPosition(x - 1, y - 1)
}

object PawnPosition {
  def fromIndex(index: Int): Option[PawnPosition] = availablePositions.lift(index)

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

  def toIndex(position: PawnPosition): Option[Int] =
    if (availablePositions.indexOf(position) == -1) None else Some(availablePositions.indexOf(position))

}
