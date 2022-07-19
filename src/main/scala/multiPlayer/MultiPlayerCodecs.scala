package multiPlayer

import io.circe.generic.semiauto
import io.circe.{Encoder, Json}
import multiPlayer.domain.{MultiplayerState, Player, Room}
import checkers.CheckersCodecs._ //todo: this import is required, bud intellij is cleaning it on the fly

object MultiPlayerCodecs {

  implicit val playerEncoder: Encoder[Player] = { player =>
    Json.fromString(player.name)
  }
  implicit val roomEncoder: Encoder[Room] = semiauto.deriveEncoder[Room]
  implicit val multiplayerStateEncoder: Encoder[MultiplayerState] = semiauto.deriveEncoder[MultiplayerState]

}
