package multiplayer.domain

import multiplayer.domain.WebsocketRoutes.ChatRoute

sealed trait InputMessage {
  val player: Player
}

case class EnterGame(player: Player)                   extends InputMessage
case class LeaveGame(player: Player)                   extends InputMessage
case class EnterRoom(player: Player, roomName: String) extends InputMessage
case class LeaveRoom(player: Player)                   extends InputMessage
case class Chat(player: Player, text: String)          extends InputMessage
case class Error(player: Player)                       extends InputMessage
case class MakeMove(
  player: Player,
  from: String,
  to: String
)                                                      extends InputMessage

object InputMessage {
  //todo: do it with startWith like in angular
  def parse(player: Player, inputText: String): InputMessage = {
    val text = inputText.replaceAll("\"", "") //todo: solve this issue in Angular, this is only on the frontend
    splitWords(text) match {
      case ("/room", roomName, "") => EnterRoom(player, roomName.toLowerCase)
      case ("/leaveRoom", "", "")  => LeaveRoom(player)
      case ("/move", from, to)     => MakeMove(player, from, to)
      case ("/chat", _, _)         => Chat(player, text.replaceFirst(ChatRoute.tag, ""))
      case _                       => Error(player)
    }
  }

  private def splitWords(text: String): (String, String, String) = {
    val (first, rest)   = splitTwo(text)
    val (second, third) = splitTwo(rest)
    (first, second, third)
  }

  private def splitTwo(text: String): (String, String) = {
    val trimmedText: String = text.trim
    val firstSpace: Int     = trimmedText.indexOf(' ')
    if (firstSpace < 0)
      (trimmedText, "")
    else
      (trimmedText.substring(0, firstSpace), trimmedText.substring(firstSpace + 1).trim)
  }
}
