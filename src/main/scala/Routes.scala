import cats._
import cats.effect._
import cats.implicits._
import org.http4s.circe._
import org.http4s._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.dsl._
import org.http4s.dsl.impl._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.server.blaze.BlazeServerBuilder
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
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "checkers" :?
          boardQueryParamMatcher(board) +&
          currentColourQueryParamMatcher(currentColour) +&
          moveFromQueryParamMatcher(moveFrom) +&
          moveToQueryParamMatcher(moveTo) =>

        val state: State = State(board, currentColour)

        Ok(state.asJson)
        }


    }


  override def run(args: List[String]): IO[ExitCode] = {

    val apis = Router(
      "/api" -> Routes.checkersRoute[IO],
    ).orNotFound

    BlazeServerBuilder[IO](global)
      .bindHttp(8083, "localhost")
      .withHttpApp(apis)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }

}
