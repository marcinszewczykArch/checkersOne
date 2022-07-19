package multiPlayer.domain

import checkers.domain.GameState

final case class Room(name: String, players: List[Player] = Nil, gameState: GameState) {}
