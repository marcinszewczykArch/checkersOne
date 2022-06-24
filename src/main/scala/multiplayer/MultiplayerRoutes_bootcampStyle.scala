package multiplayer

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.toSemigroupKOps
import fs2.concurrent.{Queue, Topic}
import fs2.{Pipe, Stream}
import org.http4s.dsl.io.{->, /, GET, Root}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.{HttpApp, HttpRoutes}

import scala.concurrent.ExecutionContext

object MultiplayerRoutes_bootcampStyle extends IOApp {

  private val echoRoute = HttpRoutes.of[IO] {

    // websocat "ws://localhost:9002/echo"
    case GET -> Root / "echo" =>

      val echoPipe: Pipe[IO, WebSocketFrame, WebSocketFrame] =
        _.collect {
          case WebSocketFrame.Text(message, _) => WebSocketFrame.Text(message)
        }

      for {
        queue <- Queue.unbounded[IO, WebSocketFrame]
        response <- WebSocketBuilder[IO].build(
          receive = queue.enqueue,
          send = queue.dequeue.through(echoPipe),
        )
      } yield response
  }

    def multiplayerRoutes(chatTopic: Topic[IO, String]): HttpRoutes[IO] = HttpRoutes.of[IO] {

      case GET -> Root / "ws" / userName => WebSocketBuilder[IO].build(
          receive = chatTopic.publish.compose[Stream[IO, WebSocketFrame]](_.collect {
            case WebSocketFrame.Text(message, _) => userName + ": " + message
          }),
          send = chatTopic.subscribe(maxQueued = 10).map(WebSocketFrame.Text(_)),
        )
    }


    def httpApp(chatTopic: Topic[IO, String]): HttpApp[IO] = {
      echoRoute <+> multiplayerRoutes(chatTopic)
    }.orNotFound

    override def run(args: List[String]): IO[ExitCode] = {

      for {
        chatTopic <- Topic[IO, String](initial = "Welcome to the chat!")
        _ <- BlazeServerBuilder[IO](ExecutionContext.global)
          .bindHttp(port = 9002, host = "localhost")
          .withHttpApp(httpApp(chatTopic))
          .serve
          .compile
          .drain
      } yield ExitCode.Success
    }

  }
