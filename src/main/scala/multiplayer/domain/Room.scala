package multiplayer.domain

final case class Room(name: String, players: List[Player] = Nil) {}
