package singlePlayer

import cats.effect.IO
import cats.implicits._
import checkers.CheckersCodecs.gameStateEncoder
import checkers.domain.{GameState, PawnMove, ValidateMove}
import io.circe.syntax.EncoderOps
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.dsl.io._
import singlePlayer.AiEasy._

object SinglePlayerRoutes {

  val singlePlayerRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root => Ok("Server is running...")

    case GET -> Root / "checkers" :?
        boardQueryParamMatcher(board) +&
        currentColourQueryParamMatcher(currentColour) +&
        nextMoveFromQueryParamMatcher(nextMoveFrom) +&
        statusQueryParamMatcher(status) +&
        moveFromQueryParamMatcher(moveFrom) +&
        moveToQueryParamMatcher(moveTo) =>
      val validationResult = for {
        state <- GameState.fromString(board, currentColour, nextMoveFrom, status)
        move  <- PawnMove.fromString(moveFrom, moveTo)
      } yield ValidateMove().apply(move, state)

      validationResult match {
        case Some(result) =>
          result match {
            case Right(newState)       => Ok(newState.asJson)
            case Left(validationError) => NotAcceptable(validationError.show)
          }
        case _            => NotAcceptable("invalid input")
      }

    case GET -> Root / "initialstate" => Ok(GameState.initial.asJson)

    case GET -> Root / "checkersAi" :?
        boardQueryParamMatcher(board) +&
        nextMoveFromQueryParamMatcher(nextMoveFrom) +&
        statusQueryParamMatcher(status) +&
        currentColourQueryParamMatcher(currentColour) =>
      GameState.fromString(board, currentColour, nextMoveFrom, status) match {
        case Some(state) =>
          Thread.sleep(500) //delay the AI move to make it easier to notice on the frontend side
          makeAiMoveMedium(state)
        case _           => NotAcceptable("invalid input")
      }
  }

  object boardQueryParamMatcher         extends QueryParamDecoderMatcher[String]("board")
  object currentColourQueryParamMatcher extends QueryParamDecoderMatcher[String]("currentColour")
  object nextMoveFromQueryParamMatcher  extends QueryParamDecoderMatcher[String]("nextMoveFrom")
  object statusQueryParamMatcher        extends QueryParamDecoderMatcher[String]("status")
  object moveFromQueryParamMatcher      extends QueryParamDecoderMatcher[String]("moveFrom")
  object moveToQueryParamMatcher        extends QueryParamDecoderMatcher[String]("moveTo")
}
