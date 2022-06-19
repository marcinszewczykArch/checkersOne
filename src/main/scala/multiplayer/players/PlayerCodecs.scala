package multiplayer.players

import io.circe.{Codec, Decoder, Encoder}
import io.circe.generic.semiauto.{deriveCodec}
import multiplayer.players.domain.{Player, PlayerId, PlayerName}
import multiplayer.MultiplayerCodecs.uuidStringCodec

object PlayerCodecs {
  implicit val playerIdCodec: Codec[PlayerId] = Codec.from(
    encodeA = uuidStringCodec.contramap[PlayerId](_.value),
    decodeA = uuidStringCodec.map(PlayerId.apply)
  )

  implicit val playerNameCodec: Codec[PlayerName] = Codec.from(
    encodeA = Encoder.encodeString.contramap[PlayerName](_.value),
    decodeA = Decoder.decodeString.map(PlayerName)
  )

implicit val playerCodec: Codec[Player] = deriveCodec

}
