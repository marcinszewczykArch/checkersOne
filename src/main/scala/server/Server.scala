package server

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, Timer}
import cats.implicits._
import database.DatabaseRoutes
import fs2.Stream
import fs2.concurrent.{Queue, Topic}
import multiPlayer.MultiPlayerRoutes
import multiPlayer.domain._
import org.http4s.HttpApp
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import singlePlayer.SinglePlayerRoutes
import scala.concurrent.duration._
import io.circe.syntax.EncoderOps

import scala.concurrent.ExecutionContext

object Server {

  def start: IO[ExitCode] = {
    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    implicit val t: Timer[IO]         = IO.timer(ExecutionContext.global)

    for {
      queue <- Queue.unbounded[IO, InputMessage]
      topic <- Topic[IO, OutputMessage](SendToUsers(List.empty, WebsocketRoutes.None, ""))
      ref   <- Ref.of[IO, MultiplayerState](MultiplayerState.initial)
      port  <- ConcurrentEffect[IO].delay(sys.env.get("PORT").flatMap(_.toIntOption).getOrElse(9000))

      exitCode <- {

        val httpStream = BlazeServerBuilder[IO](ExecutionContext.global)
          .bindHttp(port = port, "0.0.0.0")
          .withHttpApp(CORS(httpApp(ref, queue, topic)))
          .serve

        val processingStream = queue.dequeue
          .evalMap(msg => ref.modify(_.process(msg)))
          .flatMap(Stream.emits)
          .through(topic.publish)

        // Stream to keep alive idle WebSockets by sending current state to all players every 5 seconds
        val keepAlive = Stream
          .awakeEvery[IO](5.seconds)
          .map(_ => KeepAlive(ref.get.unsafeRunSync()))
          .through(topic.publish)

        Stream(httpStream, processingStream, keepAlive).parJoinUnbounded.compile.drain
          .as(ExitCode.Success)
      }
    } yield exitCode
  }

  private def httpApp(
    chatState: Ref[IO, MultiplayerState],
    queue: Queue[IO, InputMessage],
    topic: Topic[IO, OutputMessage]
  ): HttpApp[IO] = {
    SinglePlayerRoutes.singlePlayerRoutes <+> DatabaseRoutes.databaseRoutes <+> MultiPlayerRoutes.multiPlayerRoutes(
      chatState,
      queue,
      topic
    )
  }.orNotFound

}
