package checkers

import checkers.domain._
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}


object CheckersCodecs {

  implicit val pawnPositionStatusEncoder: Encoder[PawnPosition] = deriveEncoder[PawnPosition]

  implicit val pawnTypeStatusEncoder: Encoder[PawnType] = deriveEncoder[PawnType]

  implicit val pawnStatusEncoder: Encoder[Pawn]  = Encoder.instance {
    pawn => Json.fromInt(PawnPosition.availablePositions.indexOf(pawn.position))
  }

  implicit val sideStatusEncoder: Encoder[Side] = Encoder.instance {
    side => Json.fromString(side.tag)
  }

  implicit val gameStateEncoder: Encoder[GameState] = deriveEncoder[GameState]

  implicit val gameStatusEncoder: Encoder[GameStatus] = Encoder.instance {
    status =>
//      implicit val winEncoder: Encoder[GameStatus.Win] =
//        Encoder.forProduct2("tag", "by")(status => (status.tag, status.by))

      status match {
//        case status: GameStatus.Win       => status.asJson

        case status @ GameStatus.WinWhite => Json.fromFields(Iterable("tag" → status.tag.asJson))
        case status @ GameStatus.WinRed   => Json.fromFields(Iterable("tag" → status.tag.asJson))
        case status @ GameStatus.Ongoing  => Json.fromFields(Iterable("tag" → status.tag.asJson))
        case status @ GameStatus.Draw     => Json.fromFields(Iterable("tag" → status.tag.asJson))

      }
  }

  implicit val boardStatusEncoder: Encoder[Board] = Encoder.instance {
    board => Json.fromString(board.toString)
  }

}
