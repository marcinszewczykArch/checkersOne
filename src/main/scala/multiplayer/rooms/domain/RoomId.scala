package multiplayer.rooms.domain

import multiplayer.domain.UuidString

final case class RoomId(value: UuidString)

object RoomId {
  def unapply(s: String): Option[RoomId] =
    UuidString.fromString(s).map(apply).toOption
}
