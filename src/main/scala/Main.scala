import cats.effect.{ExitCode, IO, IOApp}
import server.Server

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    database.Doobie.initialSchema *> Server.start
}
