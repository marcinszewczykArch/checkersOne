package checkers.domain

case class PawnMove(from: PawnPosition, to: PawnPosition)

object PawnMove {
  def fromString(from: String, to: String): Option[PawnMove] =
    for {
      from <- PawnPosition.fromIndex(from.toInt)
      to   <- PawnPosition.fromIndex(to.toInt)
    } yield PawnMove(from, to)

}
