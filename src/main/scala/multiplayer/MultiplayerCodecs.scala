package multiplayer

import io.circe.generic.semiauto
import io.circe.{Encoder, Json}
import multiplayer.players.domain.Player
import multiplayer.rooms.domain.Room

object MultiplayerCodecs {

  implicit val playerEncoder: Encoder[Player] = {
    player => Json.fromString(player.name)
  }

  implicit val roomEncoder: Encoder[Room] = semiauto.deriveEncoder[Room]

  implicit val multiplayerStateEncoder: Encoder[MultiplayerState] = semiauto.deriveEncoder[MultiplayerState]

}
