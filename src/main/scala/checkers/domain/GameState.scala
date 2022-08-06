package checkers.domain

import checkers.domain.Side.White

final case class GameState(
  status: GameStatus = GameStatus.Ongoing,
  movesNow: Side,
  board: Board,
  nextMoveFrom: Option[PawnPosition] = None
)

object GameState {
  def initial: GameState =
    GameState(
      status = GameStatus.Ongoing,
      movesNow = White,
      board = Board.initial,
      nextMoveFrom = None
    )

  def fromString(
    board: String,
    movesNow: String,
    nextMoveFrom: String,
    status: String
  ): Option[GameState] = {

    val nextMoveFromOption: Option[PawnPosition] = for {
      index        <- nextMoveFrom.toIntOption
      pawnPosition <- PawnPosition.fromIndex(index)
    } yield pawnPosition

    for {
      movesNow   <- Side.fromString(movesNow)
      board      <- Board.fromString(board)
      gameStatus <- GameStatus.withValueOpt(status.toLowerCase)
    } yield GameState(gameStatus, movesNow, board, nextMoveFromOption)

  }

}
