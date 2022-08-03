package checkers.domain

import checkers.domain.Side.White

final case class GameState(
  status: GameStatus = GameStatus.Ongoing,
  movesNow: Side,
  board: Board,
  nextMoveBy: Option[Pawn] = None
)

object GameState {
  def initial: GameState =
    GameState(
      status = GameStatus.Ongoing,
      movesNow = White,
      board = Board.initial,
      nextMoveBy = None
    )

  //todo: remove default values from nextMoveBy and status
  def fromString(
    board: String,
    movesNow: String,
    nextMoveBy: String = "None",
    status: String = "ongoing"
  ): Option[GameState] = {

    val nextMoveByOption: Option[Pawn] = for {
      board        <- Board.fromString(board)
      index        <- nextMoveBy.toIntOption
      pawnPosition <- PawnPosition.fromIndex(index)
      pawn         <- board.pawnAt(pawnPosition)
    } yield pawn

    for {
      movesNow   <- Side.fromString(movesNow)
      board      <- Board.fromString(board)
      gameStatus <- GameStatus.withValueOpt(status.toLowerCase)
    } yield GameState(gameStatus, movesNow, board, nextMoveByOption)

  }

}
