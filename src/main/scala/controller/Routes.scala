package controller

import cats.Monad
import cats.effect.{ExitCode, IO, IOApp}
import domain.{GameState, Pawn, PawnMove}
import io.circe.Json
import org.http4s.{EmptyBody, EntityBody, Headers, HttpRoutes, HttpVersion, Response, Status}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.typelevel.vault.Vault
import service.CheckersService
import service.CheckersService.{ErrorMessage, moveEncoder, stateDecoder, stateEncoder, validateMove}

import scala.concurrent.ExecutionContext.global

object Routes extends IOApp {

  case class State(board: String, currentColour: String)

  object boardQueryParamMatcher extends QueryParamDecoderMatcher[String]("board")

  object currentColourQueryParamMatcher extends QueryParamDecoderMatcher[String]("currentColour")

  object moveFromQueryParamMatcher extends QueryParamDecoderMatcher[String]("moveFrom")

  object moveToQueryParamMatcher extends QueryParamDecoderMatcher[String]("moveTo")

  def checkersRoute[F[_] : Monad]: HttpRoutes[F] = {
    println("start---")
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] {

      case GET -> Root / "checkers" :?
          boardQueryParamMatcher(board) +&
          currentColourQueryParamMatcher(currentColour) +&
          moveFromQueryParamMatcher(moveFrom) +&
          moveToQueryParamMatcher(moveTo) =>

        val state: GameState = stateEncoder(board, currentColour)
        val move: PawnMove = moveEncoder(moveFrom, moveTo)

        validateMove(state, move) match {
          case Right(newState) => Ok(stateDecoder(newState))
          case Left(error)     => NotAcceptable(error)
        }
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {

    val apis = Router(
      "/api" -> CORS(Routes.checkersRoute[IO]),
    ).orNotFound

    BlazeServerBuilder[IO](global)
      .bindHttp(8081, "localhost")
      .withHttpApp(apis)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }

}
