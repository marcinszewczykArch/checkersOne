package server

import cats.effect.concurrent.Ref
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import checkers.CheckersCodecs.gameStateEncoder
import checkers.domain.{GameState, PawnMove, ValidateMove}
import fs2.concurrent.{Queue, Topic}
import fs2.{Pipe, Stream}
import io.circe.syntax.EncoderOps
import multiplayer.domain._
import org.http4s.circe._
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}
import org.http4s.{HttpApp, HttpRoutes}
import singleplayer.AiEasy.makeAiMove
//import io.circe.generic.auto._
import scala.concurrent.ExecutionContext

object Server extends IOApp {

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
        case Right(newState) => Ok(newState.asJson)
        case Left(validationError) => NotAcceptable(validationError.show)
      }

    case GET -> Root / "checkersAi" :?
      boardQueryParamMatcher(board) +&
        currentColourQueryParamMatcher(currentColour) =>

      val state: GameState = GameState.fromString(board, currentColour)
      Thread.sleep(2000)
      makeAiMove(state)
  }


  def multiplayerRoutes(state: Ref[IO, MultiplayerState], queue: Queue[IO, InputMessage], topic: Topic[IO, OutputMessage]): HttpRoutes[IO] =
    HttpRoutes.of[IO] {

      case GET -> Root / "ws" / playerName =>
        // Routes messages from our "topic" to a WebSocket
        val toClient: Stream[IO, WebSocketFrame.Text] = topic
          .subscribe(1000)
          .filter(_.forPlayer(Player(playerName)))
          .map(msg => Text(msg.toString))


        def processInput(wsfStream: Stream[IO, WebSocketFrame]): Stream[IO, Unit] = {
          val player: Player = Player(playerName)

          val entryStream: Stream[IO, InputMessage] = Stream.emits(Seq(Chat(player, s"Welcome in checkersOne ${player.name}!")))
          val playersInGame: Stream[IO, InputMessage] = Stream.emits(Seq(EnterGame(player)))

          val parsedWebSocketInput: Stream[IO, InputMessage] = wsfStream.collect {
            case Text(text, _) => InputMessage.parse(player, text)
            case Close(_) => LeaveGame(player)
          }

          (playersInGame ++ entryStream ++ parsedWebSocketInput).through(queue.enqueue)
        }

        val inputPipe: Pipe[IO, WebSocketFrame, Unit] = processInput

        // Build the WebSocket handler
        WebSocketBuilder[IO].build(toClient, inputPipe)
    }

  private def httpApp(chatState: Ref[IO, MultiplayerState], queue: Queue[IO, InputMessage], topic: Topic[IO, OutputMessage]): HttpApp[IO] = {
    checkersRoute <+> multiplayerRoutes(chatState, queue, topic)
  }.orNotFound

  override def run(args: List[String]): IO[ExitCode] = {

    for (
      queue <- Queue.unbounded[IO, InputMessage];
      topic <- Topic[IO, OutputMessage](SendToUsers(List.empty, WebsocketRoutes.None, ""));
      ref   <- Ref.of[IO, MultiplayerState](MultiplayerState());

      exitCode <- {
        val httpStream = BlazeServerBuilder[IO](ExecutionContext.global)
          .bindHttp(port = 9000, host = "localhost")
          .withHttpApp(CORS(httpApp(ref, queue, topic)))
          .serve

        val processingStream = queue.dequeue
          .evalMap(msg => ref.modify(_.process(msg)))
          .flatMap(Stream.emits)
          .through(topic.publish)

        // fs2 Streams must be "pulled" to process messages. Drain will perpetually pull our top-level streams
        Stream(httpStream, processingStream)
          .parJoinUnbounded
          .compile
          .drain
          .as(ExitCode.Success)
      }) yield exitCode
  }
}
