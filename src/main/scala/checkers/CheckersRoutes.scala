package checkers

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import checkers.CheckersCodecs.gameStateEncoder
import checkers.domain._
import io.circe.syntax.EncoderOps
import org.http4s.circe._
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.{HttpRoutes, Response}

import scala.annotation.tailrec
//import io.circe.generic.auto._
import scala.concurrent.ExecutionContext.global

object CheckersRoutes extends IOApp {

  case class State(board: String, currentColour: String)

  object boardQueryParamMatcher extends QueryParamDecoderMatcher[String]("board")

  object currentColourQueryParamMatcher extends QueryParamDecoderMatcher[String]("currentColour")

  object moveFromQueryParamMatcher extends QueryParamDecoderMatcher[String]("moveFrom")

  object moveToQueryParamMatcher extends QueryParamDecoderMatcher[String]("moveTo")

  val checkersRoute = HttpRoutes.of[IO] {

        case GET -> Root / "checkers" :?
            boardQueryParamMatcher(board) +&
            currentColourQueryParamMatcher(currentColour) +&
            moveFromQueryParamMatcher(moveFrom) +&
            moveToQueryParamMatcher(moveTo) =>

          val state: GameState = GameState.fromString(board, currentColour)
          val move: PawnMove = PawnMove.fromString(moveFrom, moveTo)

          ValidateMove.apply().apply(move, state) match {
            case Right(newState)        => Ok(newState.asJson)
            case Left(validationError)  => NotAcceptable(validationError.show)
          }

        case GET -> Root / "checkersAi" :?
            boardQueryParamMatcher(board) +&
            currentColourQueryParamMatcher(currentColour) =>

          val state: GameState = GameState.fromString(board, currentColour)
          Thread.sleep(2000)
          makeAiMove(state)

  }

  @tailrec
  def makeAiMove(state: GameState): IO[Response[IO]] = {
    import scala.util.Random
    val moveFrom = Random.between(0, 31).toString
    var moveTo = Random.between(0, 31).toString

    var move: PawnMove = PawnMove.fromString(moveFrom, moveTo)

      ValidateMove.apply().apply(move, state) match {
      case Right(newState)  => println(moveFrom + " -> " + moveTo); Ok(newState.asJson)
      case Left(_)          => makeAiMove(state)
    }

  }


  override def run(args: List[String]): IO[ExitCode] = {

      val httpApp = Seq(
        checkersRoute
      ).reduce(_ <+> _).orNotFound

    BlazeServerBuilder[IO](global)
      .bindHttp(9001, "localhost")
      .withHttpApp(CORS(httpApp))
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

}
