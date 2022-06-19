package multiplayer.rooms.domain

import multiplayer.players.domain.Player

final case class Room(id: RoomId, name: RoomName, players: List[Player]){


}
