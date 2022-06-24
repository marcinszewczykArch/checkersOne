package multiplayer


import cats.effect.concurrent.Ref
import cats.effect.{ContextShift, Sync}
import fs2.concurrent.{Queue, Topic}
import fs2.{Pipe, Stream}
import multiplayer.domain.UuidString
import multiplayer.players.domain.{Player, PlayerId, PlayerName}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}


class ChatRoutesNew[F[_]: Sync: ContextShift](
                                               chatState: Ref[F, ChatStateNew],
                                               queue: Queue[F, InputMessageNew],
                                               topic: Topic[F, OutputMessageNew]
                                             ) extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root / "ws" / userName =>
      // Routes messages from our "topic" to a WebSocket
      val toClient: Stream[F, WebSocketFrame.Text] = topic
        .subscribe(1000)
        .filter(_.forPlayer(Player(PlayerId(UuidString(userName)), PlayerName(userName))))
        .map(msg => Text(msg.toString))


      def processInput(wsfStream: Stream[F, WebSocketFrame]): Stream[F, Unit] = {
      val player: Player = Player(PlayerId(UuidString(userName)), PlayerName(userName))

        val entryStream: Stream[F, InputMessageNew] = Stream.emits(Seq(EnterRoom(player, InputMessageNew.DefaultRoom)))

        val parsedWebSocketInput: Stream[F, InputMessageNew] = wsfStream.collect {
          case Text(text, _) => InputMessageNew.parse(player, text)
          case Close(_) => Disconnect(player)
        }

        (entryStream ++ parsedWebSocketInput).through(queue.enqueue)
      }

      val inputPipe: Pipe[F, WebSocketFrame, Unit] = processInput

      // Build the WebSocket handler
      WebSocketBuilder[F].build(toClient, inputPipe)
  }
}