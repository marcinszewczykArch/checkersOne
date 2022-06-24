package multiplayer

import multiplayer.domain.UuidString
import multiplayer.players.domain.Player
import multiplayer.rooms.domain.{Room, RoomId, RoomName}

/*
 * Trait for any input operation that could come from the user
 */
sealed trait InputMessageNew {
  val player: Player
}
case class Chat(player: Player, text: String)                         extends InputMessageNew
case class EnterRoom(player: Player, room: Room)                      extends InputMessageNew
case class Disconnect(player: Player)                                 extends InputMessageNew

object InputMessageNew {
  val DefaultRoomName = "default"
  val DefaultRoom: Room = Room(RoomId(UuidString(DefaultRoomName)), RoomName(DefaultRoomName))

  def parse(player: Player, text: String): InputMessageNew = splitFirstTwoWords(text) match {
    case ("/room", "", "")        => EnterRoom(player, DefaultRoom)
    case ("/room", roomName, "")  => EnterRoom(player,Room(RoomId(UuidString(roomName.toLowerCase)), RoomName(roomName.toLowerCase)))
    case _                        => Chat(player, text)
  }

  private def splitFirstWord(text: String): (String, String) = {
    val trimmedText: String = text.trim
    val firstSpace: Int = trimmedText.indexOf(' ')
    if (firstSpace < 0)
      (trimmedText, "")
    else
      (trimmedText.substring(0, firstSpace), trimmedText.substring(firstSpace + 1).trim)
  }

  private def splitFirstTwoWords(text: String): (String, String, String) = {
    val (first, intermediate) = splitFirstWord(text)
    val (second, rest)        = splitFirstWord(intermediate)

    (first, second, rest)
  }
}