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
import singlePlayer.AiEasy.makeAiMove

object SinglePlayerRoutes {

  val singlePlayerRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root => Ok("Server is running...")

    //todo: GET with parameters if GameState is not saved on the server side
    case GET -> Root / "checkers" :?
        boardQueryParamMatcher(board) +&
        currentColourQueryParamMatcher(currentColour) +&
        nextMoveByQueryParamMatcher(nextMoveBy) +&
        statusQueryParamMatcher(status) +&
        moveFromQueryParamMatcher(moveFrom) +&
        moveToQueryParamMatcher(moveTo) =>
      val state: GameState = GameState.fromString(board, currentColour, nextMoveBy, status).get //todo deal with .get
      val move: PawnMove   = PawnMove.fromString(moveFrom, moveTo).get                          //todo: deal with .get

      ValidateMove.apply().apply(move, state) match {
        case Right(newState)       => Ok(newState.asJson)
        case Left(validationError) => NotAcceptable(validationError.show)
      }

    case GET -> Root / "initialstate" => Ok(GameState.initial.asJson)

    //todo: POST with body if GameState is saved on the server side (request is changing the state)
    //    case req@POST -> Root / "checkers" =>
    //      val move = req.as[PawnMove]
    //      val state: GameState = GameState.fromString(board, currentColour)
    //      val move: PawnMove = PawnMove.fromString(moveFrom, moveTo)
    //
    //      ValidateMove.apply().apply(move, state) match {
    //        case Right(newState) => Ok(newState.asJson)
    //        case Left(validationError) => NotAcceptable(validationError.show)
    //      }

    case GET -> Root / "checkersAi" :?
        boardQueryParamMatcher(board) +&
        nextMoveByQueryParamMatcher(nextMoveBy) +&
        statusQueryParamMatcher(status) +&
        currentColourQueryParamMatcher(currentColour) =>
      val state: GameState = GameState.fromString(board, currentColour, nextMoveBy, status).get //todo deal with .get
      Thread.sleep(500)
      makeAiMove(state)
  }

  object boardQueryParamMatcher         extends QueryParamDecoderMatcher[String]("board")
  object currentColourQueryParamMatcher extends QueryParamDecoderMatcher[String]("currentColour")
  object nextMoveByQueryParamMatcher    extends QueryParamDecoderMatcher[String]("nextMoveBy")
  object statusQueryParamMatcher        extends QueryParamDecoderMatcher[String]("status")
  object moveFromQueryParamMatcher      extends QueryParamDecoderMatcher[String]("moveFrom")
  object moveToQueryParamMatcher        extends QueryParamDecoderMatcher[String]("moveTo")
}
