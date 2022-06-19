package checkers

import checkers.domain._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}


object CheckersCodecs {

  implicit val pawnPositionStatusDecoder: Decoder[PawnPosition] = deriveDecoder[PawnPosition]
  implicit val pawnPositionStatusEncoder: Encoder[PawnPosition] = deriveEncoder[PawnPosition]

  implicit val pawnTypeStatusDecoder: Decoder[PawnType] = deriveDecoder[PawnType]
  implicit val pawnTypeStatusEncoder: Encoder[PawnType] = deriveEncoder[PawnType]

  implicit val pawnStatusDecoder: Decoder[Pawn] = deriveDecoder[Pawn]
  implicit val pawnStatusEncoder: Encoder[Pawn] = deriveEncoder[Pawn]

  implicit val boardStatusDecoder: Decoder[Board] = deriveDecoder[Board]
  implicit val boardStatusEncoder: Encoder[Board] = deriveEncoder[Board]

  implicit val sideStatusDecoder: Decoder[Side] = deriveDecoder[Side]
  implicit val sideStatusEncoder: Encoder[Side] = deriveEncoder[Side]

  implicit val gameStatusDecoder: Decoder[GameStatus] = deriveDecoder[GameStatus]
  implicit val gameStatusEncoder: Encoder[GameStatus] = deriveEncoder[GameStatus]

  implicit val gameStateDecoder: Decoder[GameState] = deriveDecoder[GameState]
  implicit val gameStateEncoder: Encoder[GameState] = deriveEncoder[GameState]

}
