import CheckersService.ErrorMessage
import cats._
import cats.effect._
import cats.implicits._
import org.http4s.circe._
import org.http4s._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.Json
import org.http4s.dsl._
import org.http4s.dsl.impl._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.typelevel.ci.CIString

import java.time.Year
import java.util.UUID
import scala.collection.mutable
import scala.util.Try
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

        //board to 2d array
        val state: GameState = CheckersService.stateEncoder(board, currentColour)
        val move: PawnMove = CheckersService.moveEncoder(moveFrom, moveTo)

        //validation move
        val newState: Either[ErrorMessage, GameState] =  CheckersService.validateMove(state, move)

        if (newState.isLeft)
          NotAcceptable()

        else {
          //encode to State class
          val response: Json = CheckersService.stateDecoder(newState.right.get)
          Ok(response)

//          val newBoard: String = board.split("").updated(moveFrom.toInt, "o").updated(moveTo.toInt, currentColour).mkString("")
//          val newColour: String = if (currentColour == "r") "w" else "r"
//          Ok(State(newBoard, newColour).asJson)
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
