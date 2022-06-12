package domain

case class PawnPosition(x: Int, y: Int) {

  def isOnTheBoard: Boolean = {
    x >= 0 &&
    x <= 7 &&
    y >= 0 &&
    y <= 7
  }

  def upRight(): PawnPosition = {
    PawnPosition(x+1, y+1)
  }

  def upLeft(): PawnPosition = {
    PawnPosition(x+1, y-1)
  }

  def downRight(): PawnPosition = {
    PawnPosition(x-1, y+1)
  }
  def downLeft(): PawnPosition = {
    PawnPosition(x-1, y-1)
  }


}
