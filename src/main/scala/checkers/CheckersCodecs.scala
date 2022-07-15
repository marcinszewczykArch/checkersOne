package checkers

import checkers.domain._
import io.circe.generic.semiauto.deriveEncoder
import io.circe.{Encoder, Json}

object CheckersCodecs {

  implicit val pawnPositionStatusEncoder: Encoder[PawnPosition] = deriveEncoder[PawnPosition]

  implicit val pawnTypeStatusEncoder: Encoder[PawnType] = deriveEncoder[PawnType]

  implicit val pawnStatusEncoder: Encoder[Pawn] = Encoder.instance { pawn =>
    Json.fromInt(PawnPosition.availablePositions.indexOf(pawn.position))
  }

  implicit val sideStatusEncoder: Encoder[Side] = Encoder.instance { side => Json.fromString(side.tag) }

  implicit val gameStateEncoder: Encoder[GameState] = deriveEncoder[GameState]

  implicit val gameStatusEncoder: Encoder[GameStatus] = Encoder.instance { status => Json.fromString(status.tag) }

  implicit val boardStatusEncoder: Encoder[Board] = Encoder.instance { board => Json.fromString(board.toString) }

}
