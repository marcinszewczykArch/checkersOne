package multiplayer.players.domain

import multiplayer.domain.UuidString

final case class PlayerId(value: UuidString)

object PlayerId {
  def unapply(s: String): Option[PlayerId] =
    UuidString.fromString(s).map(apply).toOption
}
