package multiplayer

import io.circe.generic.semiauto.deriveEncoder
import io.circe.{Codec, Decoder, Encoder}
import multiplayer.domain.UuidString

object MultiplayerCodecs {
  implicit val uuidStringCodec: Codec[UuidString] = Codec.from(
    encodeA = Encoder.encodeString.contramap[UuidString](_.value),
    decodeA = Decoder.decodeString.emap(UuidString.fromString)
  )

//  implicit val errorEncoder: Encoder[Error] = deriveEncoder
}
