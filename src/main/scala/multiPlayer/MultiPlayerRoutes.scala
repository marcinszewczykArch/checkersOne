package multiPlayer

import cats.effect.IO
import cats.effect.concurrent.Ref
import fs2.concurrent.{Queue, Topic}
import fs2.{Pipe, Stream}
import io.circe.syntax.EncoderOps
import multiPlayer.domain._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}

object MultiPlayerRoutes {

  def multiPlayerRoutes(
    state: Ref[IO, MultiplayerState],
    queue: Queue[IO, InputMessage],
    topic: Topic[IO, OutputMessage]
  ): HttpRoutes[IO] =
    HttpRoutes.of[IO] {

      case GET -> Root / "ws" / playerName =>
        // Routes messages from our "topic" to a WebSocket
        val toClient: Stream[IO, WebSocketFrame.Text] = topic
          .subscribe(1000)
          .filter(_.forPlayer(Player(playerName)))
          .map(msg => Text(msg.toString))

        def processInput(wsfStream: Stream[IO, WebSocketFrame]): Stream[IO, Unit] = {
          val player: Player = Player(playerName)

          val entryStream: Stream[IO, InputMessage]   =
            Stream.emits(Seq(Chat(player, s"Welcome to checkersOne ${player.name}!")))
          val playersInGame: Stream[IO, InputMessage] = Stream.emits(Seq(EnterGame(player)))

          val parsedWebSocketInput: Stream[IO, InputMessage] = wsfStream.collect {
            case Text(text, _) => InputMessage.parse(player, text)
            case Close(_)      => LeaveGame(player)
          }

          (playersInGame ++ entryStream ++ parsedWebSocketInput).through(queue.enqueue)
        }

        val inputPipe: Pipe[IO, WebSocketFrame, Unit] = processInput

        // Build the WebSocket handler
        WebSocketBuilder[IO].build(toClient, inputPipe)

      case GET -> Root / "players"         => Ok(state.get.unsafeRunSync().players.map(_.name).asJson)

      case GET -> Root / "rooms"           => Ok(state.get.unsafeRunSync().rooms.map(_.name).asJson)
    }
}
