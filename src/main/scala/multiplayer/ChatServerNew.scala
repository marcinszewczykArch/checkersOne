package multiplayer

import cats.effect._
import cats.effect.concurrent.Ref
import fs2.Stream
import fs2.concurrent.{Queue, Topic}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.duration._

object ChatServerNew extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val httpPort: Int = 9000

    for (
      queue <- Queue.unbounded[IO, InputMessageNew];
      topic <- Topic[IO, OutputMessageNew](SendToUsers(Set.empty, ""));
      ref   <- Ref.of[IO, ChatStateNew](ChatStateNew());

      exitCode <- {
        val httpStream = ServerStreamNew.stream[IO](httpPort, ref, queue, topic)
        val keepAlive  = Stream.awakeEvery[IO](30.seconds).map(_ => KeepAlive).through(topic.publish)

        val processingStream = queue.dequeue
          .evalMap(msg => ref.modify(_.process(msg)))
          .flatMap(Stream.emits)
          .through(topic.publish)

        // fs2 Streams must be "pulled" to process messages. Drain will perpetually pull our top-level streams
        Stream(httpStream, keepAlive, processingStream)
          .parJoinUnbounded
          .compile
          .drain
          .as(ExitCode.Success)
      }) yield exitCode
  }
}

object ServerStreamNew {
  // Builds a stream for HTTP events processed by our router
  def stream[F[_]: ConcurrentEffect: Timer: ContextShift](
                                                           port: Int,
                                                           chatState: Ref[F, ChatStateNew],
                                                           queue: Queue[F, InputMessageNew],
                                                           topic: Topic[F, OutputMessageNew]
                                                         ): fs2.Stream[F, ExitCode] =
    BlazeServerBuilder[F]
      .bindHttp(port, "0.0.0.0")
      .withHttpApp(
        Router(
          "/" -> new ChatRoutesNew[F](chatState, queue, topic).routes
        ).orNotFound
      )
      .serve
}
