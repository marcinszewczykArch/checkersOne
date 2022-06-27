//package multiplayer.rooms
//
//import io.circe.{Decoder, Encoder}
//import multiplayer.rooms.domain.{Room, RoomId}
//import io.circe.Codec
//import io.circe.generic.semiauto.deriveCodec
//import multiplayer.MultiplayerCodecs.uuidStringCodec
//import multiplayer.players.PlayerCodecs._
//
//object RoomCodecs {
//
//  implicit val roomIdCodec: Codec[RoomId] = Codec.from(
//    encodeA = uuidStringCodec.contramap(_.value),
//    decodeA = uuidStringCodec.map(RoomId.apply)
//  )
//
//  implicit val roomNameCodec: Codec[RoomName] = Codec.from(
//    encodeA = Encoder.encodeString.contramap(_.value),
//    decodeA = Decoder.decodeString.map(RoomName)
//  )
//
//  implicit val roomCodec: Codec[Room] = deriveCodec[Room]
//}
