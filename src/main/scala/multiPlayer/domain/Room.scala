package multiPlayer.domain

import checkers.domain.GameState

final case class Room(name: String, playerWhite: Option[Player] = None, playerRed: Option[Player] = None, gameState: GameState = GameState.initial)
