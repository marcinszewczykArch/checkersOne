package multiplayer

import cats.effect.concurrent.Ref
import cats.effect.{ContextShift, Sync}
import fs2.concurrent.{Queue, Topic}
import fs2.{Pipe, Stream}
import multiplayer.players.domain.{Player}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}


class MultiplayerRoutes[F[_]: Sync: ContextShift](
                                                   state: Ref[F, MultiplayerState],
                                                   queue: Queue[F, InputMessage],
                                                   topic: Topic[F, OutputMessage]
                                             ) extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root / "ws" / playerName =>
      // Routes messages from our "topic" to a WebSocket
      val toClient: Stream[F, WebSocketFrame.Text] = topic
        .subscribe(1000)
        .filter(_.forPlayer(Player(playerName)))
        .map(msg => Text(msg.toString))


      def processInput(wsfStream: Stream[F, WebSocketFrame]): Stream[F, Unit] = {
      val player: Player = Player(playerName)

        val entryStream: Stream[F, InputMessage]    = Stream.emits(Seq(Chat(player, s"Welcome in checkersOne ${player.name}!")))
        val playersInGame: Stream[F, InputMessage]  = Stream.emits(Seq(PlayersInGame(player)))

        val parsedWebSocketInput: Stream[F, InputMessage] = wsfStream.collect {
          case Text(text, _)  => InputMessage.parse(player, text)
          case Close(_)       => Disconnect(player)
        }

        (playersInGame ++ entryStream ++ parsedWebSocketInput).through(queue.enqueue)
      }

      val inputPipe: Pipe[F, WebSocketFrame, Unit] = processInput

      // Build the WebSocket handler
      WebSocketBuilder[F].build(toClient, inputPipe)
  }
}