package checkers.domain

final case class PawnPosition(x: Int, y: Int) {

  def isOnTheBoard: Boolean = PawnPosition.availablePositions.contains(this)

  def upRight(): PawnPosition = PawnPosition(x+1, y+1) //todo: to return either and left if it is not on the board

  def upLeft(): PawnPosition = PawnPosition(x+1, y-1)

  def downRight(): PawnPosition = PawnPosition(x-1, y+1)

  def downLeft(): PawnPosition = PawnPosition(x-1, y-1)

}

object PawnPosition {
  def availablePositions: List[PawnPosition] = List(
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

  def fromIndex(index: Int): PawnPosition = availablePositions(index)
//    index match {
//    case 0 => PawnPosition(0, 1)
//    case 1 => PawnPosition(0, 3)
//    case 2 => PawnPosition(0, 5)
//    case 3 => PawnPosition(0, 7)
//    case 4 => PawnPosition(1, 0)
//    case 5 => PawnPosition(1, 2)
//    case 6 => PawnPosition(1, 4)
//    case 7 => PawnPosition(1, 6)
//    case 8 => PawnPosition(2, 1)
//    case 9 => PawnPosition(2, 3)
//    case 10 => PawnPosition(2, 5)
//    case 11 => PawnPosition(2, 7)
//    case 12 => PawnPosition(3, 0)
//    case 13 => PawnPosition(3, 2)
//    case 14 => PawnPosition(3, 4)
//    case 15 => PawnPosition(3, 6)
//    case 16 => PawnPosition(4, 1)
//    case 17 => PawnPosition(4, 3)
//    case 18 => PawnPosition(4, 5)
//    case 19 => PawnPosition(4, 7)
//    case 20 => PawnPosition(5, 0)
//    case 21 => PawnPosition(5, 2)
//    case 22 => PawnPosition(5, 4)
//    case 23 => PawnPosition(5, 6)
//    case 24 => PawnPosition(6, 1)
//    case 25 => PawnPosition(6, 3)
//    case 26 => PawnPosition(6, 5)
//    case 27 => PawnPosition(6, 7)
//    case 28 => PawnPosition(7, 0)
//    case 29 => PawnPosition(7, 2)
//    case 30 => PawnPosition(7, 4)
//    case 31 => PawnPosition(7, 6)
  def toIndex(position: PawnPosition): Int = availablePositions.indexOf(position)
//    position match {
//    case PawnPosition(0, 1) => 0
//    case PawnPosition(0, 3) => 1
//    case PawnPosition(0, 5) => 2
//    case PawnPosition(0, 7) => 3
//    case PawnPosition(1, 0) => 4
//    case PawnPosition(1, 2) => 5
//    case PawnPosition(1, 4) => 6
//    case PawnPosition(1, 6) => 7
//    case PawnPosition(2, 1) => 8
//    case PawnPosition(2, 3) => 9
//    case PawnPosition(2, 5) => 10
//    case PawnPosition(2, 7) => 11
//    case PawnPosition(3, 0) => 12
//    case PawnPosition(3, 2) => 13
//    case PawnPosition(3, 4) => 14
//    case PawnPosition(3, 6) => 15
//    case PawnPosition(4, 1) => 16
//    case PawnPosition(4, 3) => 17
//    case PawnPosition(4, 5) => 18
//    case PawnPosition(4, 7) => 19
//    case PawnPosition(5, 0) => 20
//    case PawnPosition(5, 2) => 21
//    case PawnPosition(5, 4) => 22
//    case PawnPosition(5, 6) => 23
//    case PawnPosition(6, 1) => 24
//    case PawnPosition(6, 3) => 25
//    case PawnPosition(6, 5) => 26
//    case PawnPosition(6, 7) => 27
//    case PawnPosition(7, 0) => 28
//    case PawnPosition(7, 2) => 29
//    case PawnPosition(7, 4) => 30
//    case PawnPosition(7, 6) => 31
//  }
}
