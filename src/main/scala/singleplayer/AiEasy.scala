package singleplayer

import cats.effect.IO
import checkers.CheckersCodecs.gameStateEncoder
import checkers.domain.{GameState, PawnMove, ValidateMove}
import io.circe.syntax.EncoderOps
import org.http4s.Response
import org.http4s.circe._
import org.http4s.dsl.io._

import scala.annotation.tailrec
object AiEasy {

  @tailrec
  def makeAiMove(state: GameState): IO[Response[IO]] = {
    import scala.util.Random
    val moveFrom = Random.between(0, 31).toString
    val moveTo = Random.between(0, 31).toString

    val move: PawnMove = PawnMove.fromString(moveFrom, moveTo)

    ValidateMove.apply().apply(move, state) match {
      case Right(newState)  => println(moveFrom + " -> " + moveTo); Ok(newState.asJson)
      case Left(_)          => makeAiMove(state)
    }
  }

}
