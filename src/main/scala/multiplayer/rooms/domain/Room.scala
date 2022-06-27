package multiplayer.rooms.domain

import multiplayer.players.domain.Player

final case class Room(name: String, players: List[Player] = Nil){


}
