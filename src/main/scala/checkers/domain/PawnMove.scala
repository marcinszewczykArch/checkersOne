package checkers.domain

case class PawnMove(from: PawnPosition, to: PawnPosition)

object PawnMove {
  def fromString(from: String, to: String): PawnMove =
    PawnMove(PawnPosition.fromIndex(from.toInt), PawnPosition.fromIndex(to.toInt))
}
