package multiplayer

import multiplayer.players.domain.Player

sealed trait InputMessage {
  val player: Player
}

case class EnterGame(player: Player)                                  extends InputMessage
case class LeaveGame(player: Player)                                  extends InputMessage
case class EnterRoom(player: Player, roomName: String)                extends InputMessage
case class LeaveRoom(player: Player)                                  extends InputMessage
case class Chat(player: Player, text: String)                         extends InputMessage
//case class ListRooms(player: Player)                                  extends InputMessage
//case class ListPlayers(player: Player)                                extends InputMessage
case class Error(player: Player)                                      extends InputMessage
case class MakeMove(player: Player, board: String, colour: String,
                    from: String, to: String)                         extends InputMessage


object InputMessage {

  def parse(player: Player, inputText: String): InputMessage = {
    val text = inputText.replaceAll("\"", "") //todo: solve this issue in Angular, this is only on the frontend
    splitWords(text) match {
      case ("/room", roomName, "", "", "")                   => EnterRoom(player, roomName.toLowerCase)
      case ("/leaveRoom", "", "", "", "")                    => LeaveRoom(player)
//      case ("/rooms", _, _, _, _)                            => ListRooms(player)
//      case ("/players", _, _, _, _)                          => ListPlayers(player)
      case ("/move", board, colour, from, to)                => MakeMove(player, board, colour, from, to)
      case ("/chat", _, _, _, _)                             => Chat(player, text.substring(6))
      case _                                                 => Error(player)
    }
  }

  private def splitTwo(text: String): (String, String) = {
    val trimmedText: String = text.trim
    val firstSpace: Int = trimmedText.indexOf(' ')
    if (firstSpace < 0)
      (trimmedText, "")
    else
      (trimmedText.substring(0, firstSpace), trimmedText.substring(firstSpace + 1).trim)
  }

  private def splitWords(text: String): (String, String, String, String, String) = {
    val (first, restOne)    = splitTwo(text)
    val (second, restTwo)   = splitTwo(restOne)
    val (third, restThree)  = splitTwo(restTwo)
    val (fourth, fifth)     = splitTwo(restThree)

    (first, second, third, fourth, fifth)
  }
}