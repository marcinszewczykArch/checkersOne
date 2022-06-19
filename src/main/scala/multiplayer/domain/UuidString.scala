package multiplayer.domain

import cats.effect.Sync

import java.util.UUID
import scala.util.Try

final case class UuidString private (value: String) extends AnyVal

object UuidString {
  def of[F[_]: Sync]: F[UuidString] =
    Sync[F].delay(UuidString(UUID.randomUUID().toString))

  def fromString(s: String): Either[String, UuidString] =
    Try(
      UuidString(UUID.fromString(s).toString)
    ).toEither.left.map(_ => "Invalid UUID")
}
