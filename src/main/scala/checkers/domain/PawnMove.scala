package checkers.domain

case class PawnMove(from: PawnPosition, to: PawnPosition) {
  //validation
}

  object PawnMove {
    def fromString(from: String, to: String): PawnMove =
      PawnMove(PawnPosition.fromIndex(from.toInt), PawnPosition.fromIndex(to.toInt))
}
