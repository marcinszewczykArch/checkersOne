package checkers

import checkers.domain._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}

object CheckersCodecs {

  implicit val pawnPositionEncoder: Encoder[PawnPosition] = deriveEncoder[PawnPosition]
  implicit val pawnPositionDecoder: Decoder[PawnPosition] = Decoder.decodeInt.emap(PawnPosition.fromIndex(_).toRight("Invalid PawnPosition"))

  implicit val pawnTypeEncoder: Encoder[PawnType] = deriveEncoder[PawnType]
  implicit val pawnTypeDecoder: Decoder[PawnType] = Decoder.decodeString.emap(PawnType.fromString(_).toRight("Invalid PawnType"))

  implicit val pawnEncoder: Encoder[Pawn] = Encoder.instance {pawn => Json.fromInt(PawnPosition.availablePositions.indexOf(pawn.position))}
  implicit val pawnDecoder: Decoder[Pawn] = deriveDecoder[Pawn]

  implicit val sideStatusEncoder: Encoder[Side] = Encoder.instance { side => Json.fromString(side.tag) }
  implicit val sideStatusDecoder: Decoder[Side] = Decoder.decodeString.emap(Side.fromString(_).toRight("Invalid Side"))


  implicit val gameStateEncoder: Encoder[GameState] = deriveEncoder[GameState]
  implicit val gameStateDecoder: Decoder[GameState] = deriveDecoder[GameState]

  implicit val gameStatusEncoder: Encoder[GameStatus] = Encoder.instance { status => Json.fromString(status.tag) }
  implicit val gameStatusDecoder: Decoder[GameStatus] = Decoder.decodeString.emap(GameStatus.fromString(_).toRight("Invalid GameStatus"))

  implicit val boardEncoder: Encoder[Board] = Encoder.instance { board => Json.fromString(board.toString) }
  implicit val boardDecoder: Decoder[Board] = Decoder.decodeString.emap(Board.fromString(_).toRight("Invalid Board"))

}
